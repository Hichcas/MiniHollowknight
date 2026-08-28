package Controller;

import Model.GameSaveData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AchievementManager {

    public interface Listener {
        void onAchievementUnlocked(String achievementId);
    }

    public static final String COMPLETION = "ach_completion";
    public static final String SPEEDRUN = "ach_speedrun";
    public static final String TRUE_HUNTER = "ach_true_hunter";
    public static final String FALSE_KNIGHT = "ach_false_knight";
    public static final String CUSTOM = "ach_custom";
    private static final String CHARM_FOUND_PREFIX = "ach_charm_found_";

    public static String charmFoundId(Model.Enums.CharmState charm) {
        return CHARM_FOUND_PREFIX + charm.name();
    }

    private final SaveManager saveManager;
    private final int slotId;
    private final Set<String> unlockedThisSlot = new HashSet<>();
    private final List<Listener> listeners = new ArrayList<>();

    public AchievementManager(SaveManager saveManager, int slotId) {
        this.saveManager = saveManager;
        this.slotId = slotId;

        GameSaveData existing = saveManager.load(slotId);
        if (existing != null && existing.unlockedAchievementsCsv != null) {
            for (String id : existing.unlockedAchievementsCsv.split(",")) {
                String trimmed = id.trim();
                if (!trimmed.isEmpty()) {
                    unlockedThisSlot.add(trimmed);
                }
            }
        }
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isUnlockedInThisSlot(String achievementId) {
        return unlockedThisSlot.contains(achievementId);
    }

    public String getUnlockedCsv() {
        return String.join(",", unlockedThisSlot);
    }

    public void unlock(String achievementId) {
        if (unlockedThisSlot.contains(achievementId)) {
            return;
        }
        unlockedThisSlot.add(achievementId);
        saveManager.unlockAchievement(slotId, achievementId);

        for (Listener listener : listeners) {
            listener.onAchievementUnlocked(achievementId);
        }
    }
}
