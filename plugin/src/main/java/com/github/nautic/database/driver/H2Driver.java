package com.github.nautic.database.driver;

import com.github.nautic.AtlasLang;
import com.github.nautic.database.Database;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public final class H2Driver implements Database {

    private final AtlasLang plugin;
    private HikariDataSource dataSource;

    public H2Driver(AtlasLang plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        File file = new File(plugin.getDataFolder(), "database");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:file:" + file.getAbsolutePath() + ";MODE=MySQL;DATABASE_TO_UPPER=false");
        config.setDriverClassName("com.github.nautic.libs.h2.Driver");
        config.setPoolName("AtlasLang-H2");
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);
        plugin.getLogger().info("Connected to H2 database.");
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
            plugin.getLogger().severe("H2 table creation failed: " + e.getMessage());
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
                "MERGE INTO AtlasLang (uuid) KEY(uuid) VALUES (?)")) {
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
