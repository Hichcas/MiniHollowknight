package Controller;

import Model.GameSaveData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaveManager {

    private static final String DB_URL = "jdbc:sqlite:save.db";
    public static final int SLOT_COUNT = 4;

    public SaveManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("", e);
        }
        initDatabase();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initDatabase() {
        String createSaves =
            "CREATE TABLE IF NOT EXISTS saves (\n" +
                "    slot_id INTEGER PRIMARY KEY,\n" +
                "    save_name TEXT,\n" +
                "    timestamp_millis INTEGER,\n" +
                "    health INTEGER,\n" +
                "    max_health INTEGER,\n" +
                "    soul INTEGER,\n" +
                "    max_soul INTEGER,\n" +
                "    current_level_id TEXT,\n" +
                "    pos_x REAL,\n" +
                "    pos_y REAL,\n" +
                "    last_safe_place_id TEXT,\n" +
                "    respawn_x REAL,\n" +
                "    respawn_y REAL,\n" +
                "    false_knight_defeated INTEGER,\n" +
                "    crystal_guardian_defeated INTEGER,\n" +
                "    zote_defeated INTEGER,\n" +
                "    winged_sentry_defeated INTEGER,\n" +
                "    equipped_charms_csv TEXT\n" +
                ");";

        String createAchievements =
            "CREATE TABLE IF NOT EXISTS save_achievements (\n" +
                "    slot_id INTEGER NOT NULL,\n" +
                "    achievement_id TEXT NOT NULL,\n" +
                "    PRIMARY KEY (slot_id, achievement_id),\n" +
                "    FOREIGN KEY (slot_id) REFERENCES saves(slot_id) ON DELETE CASCADE\n" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
            stmt.execute(createSaves);
            stmt.execute(createAchievements);
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public void save(GameSaveData data) {
        if (data.slotId < 1 || data.slotId > SLOT_COUNT) {
            throw new IllegalArgumentException("");
        }

        String sql =
            "INSERT OR REPLACE INTO saves\n" +
                "(slot_id, save_name, timestamp_millis, health, max_health, soul, max_soul,\n" +
                " current_level_id, pos_x, pos_y, last_safe_place_id, respawn_x, respawn_y,\n" +
                " false_knight_defeated, crystal_guardian_defeated, zote_defeated, winged_sentry_defeated,\n" +
                " equipped_charms_csv)\n" +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, data.slotId);
            ps.setString(2, data.saveName);
            ps.setLong(3, data.timestampMillis);
            ps.setInt(4, data.health);
            ps.setInt(5, data.maxHealth);
            ps.setInt(6, data.soul);
            ps.setInt(7, data.maxSoul);
            ps.setString(8, data.currentLevelId);
            ps.setFloat(9, data.posX);
            ps.setFloat(10, data.posY);
            ps.setString(11, data.lastSafePlaceId);
            ps.setFloat(12, data.respawnX);
            ps.setFloat(13, data.respawnY);
            ps.setInt(14, data.falseKnightDefeated ? 1 : 0);
            ps.setInt(15, data.crystalGuardianDefeated ? 1 : 0);
            ps.setInt(16, data.zoteDefeated ? 1 : 0);
            ps.setInt(17, data.wingedSentryDefeated ? 1 : 0);
            ps.setString(18, data.equippedCharmsCsv);

            ps.executeUpdate();

            saveAchievements(conn, data.slotId, data.unlockedAchievementsCsv);

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    private void saveAchievements(Connection conn, int slotId, String achievementsCsv) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement(
            "DELETE FROM save_achievements WHERE slot_id = ?")) {
            del.setInt(1, slotId);
            del.executeUpdate();
        }

        if (achievementsCsv == null || achievementsCsv.isBlank()) {
            return;
        }

        String insertSql = "INSERT INTO save_achievements (slot_id, achievement_id) VALUES (?, ?);";
        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
            for (String achievementId : achievementsCsv.split(",")) {
                String trimmed = achievementId.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                ins.setInt(1, slotId);
                ins.setString(2, trimmed);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    public GameSaveData load(int slotId) {
        String sql = "SELECT * FROM saves WHERE slot_id = ?;";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, slotId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                GameSaveData data = new GameSaveData();
                data.slotId = rs.getInt("slot_id");
                data.saveName = rs.getString("save_name");
                data.timestampMillis = rs.getLong("timestamp_millis");
                data.health = rs.getInt("health");
                data.maxHealth = rs.getInt("max_health");
                data.soul = rs.getInt("soul");
                data.maxSoul = rs.getInt("max_soul");
                data.currentLevelId = rs.getString("current_level_id");
                data.posX = rs.getFloat("pos_x");
                data.posY = rs.getFloat("pos_y");
                data.lastSafePlaceId = rs.getString("last_safe_place_id");
                data.respawnX = rs.getFloat("respawn_x");
                data.respawnY = rs.getFloat("respawn_y");
                data.falseKnightDefeated = rs.getInt("false_knight_defeated") == 1;
                data.crystalGuardianDefeated = rs.getInt("crystal_guardian_defeated") == 1;
                data.zoteDefeated = rs.getInt("zote_defeated") == 1;
                data.wingedSentryDefeated = rs.getInt("winged_sentry_defeated") == 1;
                data.equippedCharmsCsv = rs.getString("equipped_charms_csv");
                data.unlockedAchievementsCsv = loadAchievementsCsv(conn, slotId);

                return data;
            }

        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    private String loadAchievementsCsv(Connection conn, int slotId) throws SQLException {
        String sql = "SELECT achievement_id FROM save_achievements WHERE slot_id = ?;";
        List<String> ids = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getString("achievement_id"));
                }
            }
        }

        return String.join(",", ids);
    }

    public List<GameSaveData> loadAllSlots() {
        List<GameSaveData> result = new ArrayList<>();
        for (int i = 1; i <= SLOT_COUNT; i++) {
            result.add(load(i));
        }
        return result;
    }

    public void deleteSlot(int slotId) {
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM saves WHERE slot_id = ?;")) {
            ps.setInt(1, slotId);
            ps.executeUpdate();

            try (PreparedStatement ps2 = conn.prepareStatement(
                "DELETE FROM save_achievements WHERE slot_id = ?;")) {
                ps2.setInt(1, slotId);
                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public boolean slotExists(int slotId) {
        return load(slotId) != null;
    }

    public void unlockAchievement(int slotId, String achievementId) {
        try (Connection conn = connect()) {
            try (PreparedStatement ensureRow = conn.prepareStatement(
                "INSERT OR IGNORE INTO saves (slot_id, save_name, timestamp_millis) VALUES (?, ?, ?);")) {
                ensureRow.setInt(1, slotId);
                ensureRow.setString(2, "Save " + slotId);
                ensureRow.setLong(3, System.currentTimeMillis());
                ensureRow.executeUpdate();
            }

            try (PreparedStatement ins = conn.prepareStatement(
                "INSERT OR IGNORE INTO save_achievements (slot_id, achievement_id) VALUES (?, ?);")) {
                ins.setInt(1, slotId);
                ins.setString(2, achievementId);
                ins.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }

    public boolean isAchievementUnlockedInAnySlot(String achievementId) {
        String sql = "SELECT 1 FROM save_achievements WHERE achievement_id = ? LIMIT 1;";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, achievementId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("", e);
        }
    }
}
