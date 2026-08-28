package Controller;

import Model.Enums.CharmState;
import Model.GameSaveData;
import Model.Knight;

public class SaveController {

    private final SaveManager saveManager = new SaveManager();

    public void saveCurrentGame(int slotId, Knight knight, CharmManager charmManager,
                                 String currentLevelId, float respawnX, float respawnY,
                                 String lastSafePlaceId,
                                 boolean falseKnightDefeated, boolean crystalGuardianDefeated,
                                 boolean zoteDefeated, boolean wingedSentryDefeated,
                                 String unlockedAchievementsCsv) {

        GameSaveData data = new GameSaveData();
        data.slotId = slotId;
        data.saveName = "Save " + slotId;
        data.timestampMillis = System.currentTimeMillis();

        data.health = knight.getHealth();
        data.maxHealth = knight.getMaxHealth();
        data.soul = knight.getSoul();
        data.maxSoul = knight.getMaxSoul();

        data.currentLevelId = currentLevelId;
        data.posX = knight.getBody().getPosition().x;
        data.posY = knight.getBody().getPosition().y;

        data.lastSafePlaceId = lastSafePlaceId;
        data.respawnX = respawnX;
        data.respawnY = respawnY;

        data.falseKnightDefeated = falseKnightDefeated;
        data.crystalGuardianDefeated = crystalGuardianDefeated;
        data.zoteDefeated = zoteDefeated;
        data.wingedSentryDefeated = wingedSentryDefeated;

        data.equippedCharmsCsv = equippedCharmsToCsv(charmManager);
        data.unlockedAchievementsCsv = unlockedAchievementsCsv;

        saveManager.save(data);
    }

    private String equippedCharmsToCsv(CharmManager charmManager) {
        StringBuilder sb = new StringBuilder();
        for (CharmState state : CharmState.values()) {
            if (charmManager.isEquipped(state)) {
                if (sb.length() > 0) sb.append(",");
                sb.append(state.name());
            }
        }
        return sb.toString();
    }

    public GameSaveData loadGame(int slotId) {
        return saveManager.load(slotId);
    }

    public java.util.List<GameSaveData> loadAllSlots() {
        return saveManager.loadAllSlots();
    }

    public void deleteSlot(int slotId) {
        saveManager.deleteSlot(slotId);
    }

    public void applyLoadedCharms(CharmManager charmManager, String equippedCharmsCsv) {
        if (equippedCharmsCsv == null || equippedCharmsCsv.isBlank()) return;
        for (String name : equippedCharmsCsv.split(",")) {
            String trimmed = name.trim();
            if (trimmed.isEmpty()) continue;
            try {
                CharmState state = CharmState.valueOf(trimmed);
                if (!charmManager.isEquipped(state)) {
                    charmManager.toggle(state);
                }
            } catch (IllegalArgumentException ignored) {

            }
        }
    }
}
