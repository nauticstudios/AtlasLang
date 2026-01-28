package com.github.nautic.database;

import java.util.UUID;

public interface Database {
    void connect();
    void load();
    void close();

    void setLanguagePlayer(UUID uuid, String language);
    String getLanguagePlayer(UUID uuid);

}