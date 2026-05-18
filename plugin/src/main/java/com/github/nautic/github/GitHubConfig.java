package com.github.nautic.github;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class GitHubConfig {

    public final String repository;
    public final String branch;

    public final AuthType authType;
    public final String token;

    public final String remoteRoot;
    public final String localRoot;

    public final boolean createMissing;
    public final boolean overwriteExisting;
    public final boolean deleteMissing;

    public final boolean reloadAfterSync;

    public enum AuthType {
        NONE,
        TOKEN
    }

    private GitHubConfig(
            String repository,
            String branch,
            AuthType authType,
            String token,
            String remoteRoot,
            String localRoot,
            boolean createMissing,
            boolean overwriteExisting,
            boolean deleteMissing,
            boolean reloadAfterSync
    ) {
        this.repository = repository;
        this.branch = branch;
        this.authType = authType;
        this.token = token;
        this.remoteRoot = remoteRoot;
        this.localRoot = localRoot;
        this.createMissing = createMissing;
        this.overwriteExisting = overwriteExisting;
        this.deleteMissing = deleteMissing;
        this.reloadAfterSync = reloadAfterSync;
    }

    public static GitHubConfig load(FileConfiguration cfg) {
        ConfigurationSection root = cfg.getConfigurationSection("github");
        if (root == null) {
            throw new IllegalStateException("Missing 'github' section in config.yml");
        }

        String repository = string(root, "repository.name", "owner/repo");
        String branch = string(root, "repository.branch", "main");

        AuthType auth = parseAuth(string(root, "authentication.type", "none"));
        String token = string(root, "authentication.token", "");

        String remoteRoot = string(root, "paths.remote-root", "languages");
        String localRoot = string(root, "paths.local-root", "languages");

        boolean createMissing = root.getBoolean("sync.create-missing", true);
        boolean overwriteExisting = root.getBoolean("sync.overwrite-existing", true);
        boolean deleteMissing = root.getBoolean("sync.delete-missing", false);
        boolean reloadAfterSync = root.getBoolean("sync.reload-after-sync", true);

        return new GitHubConfig(
                repository, branch, auth, token,
                remoteRoot, localRoot,
                createMissing, overwriteExisting, deleteMissing, reloadAfterSync
        );
    }

    private static String string(ConfigurationSection section, String path, String fallback) {
        String value = section.getString(path);
        return (value == null || value.isEmpty()) ? fallback : value;
    }

    private static AuthType parseAuth(String raw) {
        if (raw == null) return AuthType.NONE;
        try {
            return AuthType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AuthType.NONE;
        }
    }
}
