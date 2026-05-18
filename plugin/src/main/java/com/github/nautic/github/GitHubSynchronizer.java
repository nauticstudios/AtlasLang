package com.github.nautic.github;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class GitHubSynchronizer {

    private static final int BUFFER_SIZE = 8192;

    private final GitHubConfig cfg;
    private final File pluginFolder;

    private final Set<String> remoteFiles = new HashSet<>();
    private int changedFiles = 0;
    private int deletedFiles = 0;

    public GitHubSynchronizer(GitHubConfig cfg, File pluginFolder) {
        this.cfg = cfg;
        this.pluginFolder = pluginFolder;
    }

    public GitHubSyncResult execute() throws IOException {
        File zipFile = null;
        try {
            zipFile = downloadRepositoryZip();
            if (zipFile == null) return GitHubSyncResult.FAILED;

            File localRoot = new File(pluginFolder, cfg.localRoot);
            if (!localRoot.exists() && !localRoot.mkdirs()) {
                return GitHubSyncResult.FAILED;
            }

            unzipAndSync(zipFile, localRoot);

            if (cfg.deleteMissing) {
                deleteMissingLocalFiles(localRoot);
            }

            if (changedFiles == 0 && deletedFiles == 0) {
                return GitHubSyncResult.NO_CHANGES;
            }
            return GitHubSyncResult.SUCCESS;

        } finally {
            if (zipFile != null && zipFile.exists()) {
                if (!zipFile.delete()) {
                    zipFile.deleteOnExit();
                }
            }
        }
    }

    private File downloadRepositoryZip() throws IOException {
        String url = "https://github.com/"
                + cfg.repository
                + "/archive/refs/heads/"
                + cfg.branch
                + ".zip";

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("User-Agent", "AtlasLang");

        if (cfg.authType == GitHubConfig.AuthType.TOKEN && !cfg.token.isEmpty()) {
            conn.setRequestProperty("Authorization", "token " + cfg.token);
        }

        if (conn.getResponseCode() != 200) {
            throw new IOException("GitHub HTTP " + conn.getResponseCode());
        }

        File tempZip = new File(pluginFolder, "github_sync.zip");

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, tempZip.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        return tempZip;
    }

    private void unzipAndSync(File zip, File localRoot) throws IOException {
        String canonicalRoot = localRoot.getCanonicalPath();

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();

                int firstSlash = name.indexOf('/');
                if (firstSlash == -1) continue;
                name = name.substring(firstSlash + 1);

                if (!name.startsWith(cfg.remoteRoot + "/")) continue;

                String relativePath = name.substring(cfg.remoteRoot.length() + 1);
                if (relativePath.isEmpty()) continue;

                File target = new File(localRoot, relativePath);

                String canonicalTarget = target.getCanonicalPath();
                if (!canonicalTarget.equals(canonicalRoot)
                        && !canonicalTarget.startsWith(canonicalRoot + File.separator)) {
                    throw new IOException("Blocked Zip Slip attempt: " + relativePath);
                }

                remoteFiles.add(relativePath.replace("\\", "/"));

                if (entry.isDirectory()) {
                    if (!target.exists()) target.mkdirs();
                    continue;
                }

                File parent = target.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();

                if (target.exists() && !cfg.overwriteExisting) continue;
                if (!target.exists() && !cfg.createMissing) continue;

                byte[] newContent = readEntry(zis);

                if (target.exists()) {
                    byte[] existing = Files.readAllBytes(target.toPath());
                    if (Arrays.equals(existing, newContent)) {
                        continue;
                    }
                }

                Files.write(target.toPath(), newContent);
                changedFiles++;
            }
        }
    }

    private byte[] readEntry(ZipInputStream zis) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;
        while ((len = zis.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        return out.toByteArray();
    }

    private void deleteMissingLocalFiles(File root) {
        walkAndDelete(root, "");
    }

    private void walkAndDelete(File file, String relative) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children == null) return;
            for (File child : children) {
                walkAndDelete(child,
                        relative.isEmpty()
                                ? child.getName()
                                : relative + "/" + child.getName());
            }
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".yml")) return;

        if (!remoteFiles.contains(relative.replace("\\", "/"))) {
            if (file.delete()) deletedFiles++;
        }
    }

    public int getChangedFiles() {
        return changedFiles;
    }

    public int getDeletedFiles() {
        return deletedFiles;
    }
}
