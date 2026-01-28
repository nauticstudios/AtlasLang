package com.github.nautic.github;

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
        return new GitHubConfig(
                cfg.getString("github.repository.name"),
                cfg.getString("github.repository.branch"),
                AuthType.valueOf(
                        cfg.getString("github.authentication.type").toUpperCase()
                ),
                cfg.getString("github.authentication.token"),
                cfg.getString("github.paths.remote-root"),
                cfg.getString("github.paths.local-root"),
                cfg.getBoolean("github.sync.create-missing"),
                cfg.getBoolean("github.sync.overwrite-existing"),
                cfg.getBoolean("github.sync.delete-missing"),
                cfg.getBoolean("github.sync.reload-after-sync")
        );
    }
}
