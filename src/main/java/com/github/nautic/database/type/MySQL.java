package com.github.nautic.database.type;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;

public class MySQL implements Database {

    private final AtlasLang plugin;
    private final String host, database, username, password;
    private final int port;

    private HikariDataSource dataSource;

    public MySQL(AtlasLang plugin, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    @Override
    public void connect() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&characterEncoding=utf8");
        config.setUsername(username);
        config.setPassword(password);

        config.setPoolName("AtlasLang-MySQL");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to MySQL.");
    }

    @Override
    public void load() {
        String sql = """
                CREATE TABLE IF NOT EXISTS AtlasLang (
                    uuid VARCHAR(36) PRIMARY KEY,
                    language VARCHAR(64)
                )
                """;

        try (Connection con = dataSource.getConnection();
             Statement stmt = con.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            plugin.getLogger().severe("MySQL table creation failed: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    private void ensurePlayer(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO AtlasLang (uuid) VALUES (?)")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        }
    }

    @Override
    public void setLanguagePlayer(UUID uuid, String language) {
        try (Connection con = dataSource.getConnection()) {
            ensurePlayer(con, uuid);
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE AtlasLang SET language=? WHERE uuid=?")) {
                ps.setString(1, language);
                ps.setString(2, uuid.toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to set language: " + e.getMessage());
        }
    }

    @Override
    public String getLanguagePlayer(UUID uuid) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT language FROM AtlasLang WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("language") : null;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get language: " + e.getMessage());
            return null;
        }
    }
}
