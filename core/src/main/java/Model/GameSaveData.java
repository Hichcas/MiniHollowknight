package Model;

public class GameSaveData {

    public int slotId;
    public String saveName;
    public long timestampMillis;

    public int health;
    public int maxHealth;
    public int soul;
    public int maxSoul;

    public String currentLevelId;
    public float posX;
    public float posY;

    public String lastSafePlaceId;
    public float respawnX;
    public float respawnY;

    public boolean falseKnightDefeated;
    public boolean crystalGuardianDefeated;
    public boolean zoteDefeated;
    public boolean wingedSentryDefeated;

    public String equippedCharmsCsv;

    public String unlockedAchievementsCsv;

    public GameSaveData() {

    }
}
