package View.Screen;

import Controller.*;
import Model.*;
import Model.Enums.GameState;
import Model.Enums.KnightState;
import View.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import io.github.some_example_name.lwjgl3.Main;

public class GameScreen implements Screen {
    private static final float TILE_SIZE = 16f;
    private final float deadZoneWidth = 4f;
    private final float deadZoneHeight = 2.5f;
    private final float cameraLerp = 5f;
    private float lookCameraOffsetY = 0f;
    private final float lookCameraOffsetStrength = 0.9f;
    private float cameraShakeTime = 0f;
    private float cameraShakePower = 0f;
    private final Main game;
    private World world;
    private SpriteBatch batch;
    private com.badlogic.gdx.graphics.Texture rainTexture;
    private View.RainEffect rainEffect;
    private com.badlogic.gdx.audio.Music rainAmbience;
    private boolean rainAmbiencePlaying = false;
    private static final String RAIN_AMBIENCE_PATH = "music/rain_ambience.WAV";
    private AudioManager audioManager;
    private int lastNailSlashSequencePlayed = -1;
    private int lastKnightSoul = 0;
    private boolean wasCastingFocus = false;
    private OrthographicCamera camera;
    private LevelLoader levelLoader;
    private LevelData levelData;
    private TileMapView tileMapView;
    private final Vector2 cameraTarget = new Vector2();
    private final Vector2 spawnPoint = new Vector2();
    private final Vector2 respawnPoint = new Vector2();
    private Knight knight;
    private KnightController knightController;
    private KnightAnimations knightAnimations;
    private KnightView knightView;
    private MosscreepAnimations mosscreepAnimations;
    private WingedSentryAnimations wingedSentryAnimations;
    private HuskHornheadAnimations huskHornheadAnimations;
    private CrystalGuardianAnimations crystalGuardianAnimations;
    private FalseKnightAnimations falseKnightAnimations;
    private SlashEffectAnimation slashEffectAnimations;
    private Animation<TextureRegion> slashEffectAnimation;
    private JumpWaveAnimation jumpWaveAnimations;
    private Animation<TextureRegion> jumpWaveAnimation;
    private JumpWaveAnimation falseKnightJumpWaveAnimations;
    private Animation<TextureRegion> falseKnightJumpWaveAnimation;
    private DashEffectAnimation dashEffectAnimations;
    private Animation<TextureRegion> dashEffectAnimation;
    private Texture crystalLaserTexture;
    private Model.Zote zote;
    private Controller.ZoteController zoteController;
    private Model.DialogueBox zoteDialogueBox;
    private ZoteAudio zoteAudio;
    private ZoteAnimations zoteAnimations;
    private ZoteView zoteView;
    private ZoteDialogueView zoteDialogueView;
    private EnemyManager enemyManager;
    private CombatVfxController combatVfxController;
    private SpellController spellController;
    private VengefulSpiritEffectAnimation vengefulSpiritEffectAnimations;
    private Animation<TextureRegion> vengefulSpiritEffectAnimation;
    private HowlingWraithsEffectAnimation howlingWraithsEffectAnimations;
    private Animation<TextureRegion> howlingWraithsEffectAnimation;
    private PauseOverlay pauseOverlay;
    private HUDScreen hudScreen;
    private PlayerLifecycleController playerLifecycleController;
    private Preferences prefs;
    private Music currentMusic;
    private String currentTrackPath = "";
    private GameState currentState = GameState.RUNNING;
    private BloodSplashAnimation bloodSplashAnimations;
    private Animation<TextureRegion> bloodSplashAnimation;
    private CharmManager charmManager;
    private boolean initialized = false;
    private final SaveController saveController = new SaveController();
    private final SaveManager saveManager = new SaveManager();
    private AchievementManager achievementManager;
    private AchievementPopupView achievementPopupView;
    private Controller.CrackedWallController crackedWallController;
    private View.CrackedWallAnimations crackedWallAnimations;
    private View.CrackedWallView crackedWallView;
    private boolean killedMosscreepOnce = false;
    private boolean killedWingedSentryOnce = false;
    private boolean killedHuskHornheadOnce = false;
    private boolean killedCrystalGuardianOnce = false;
    private int activeSlot = 0;
    private static final String CURRENT_LEVEL_ID = "GreenPath";

    private int deathCount = 0;
    private int enemiesKilledCount = 0;
    private float elapsedSeconds = 0f;
    private boolean gameWon = false;
    private boolean victoryAlreadyTriggered = false;
    private final java.util.Set<Object> countedDeadEnemies =
        java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
    private BossVictoryOverlay endGamePopup;

    public GameScreen(Main game) {
        this.game = game;
    }

    public GameScreen(Main game, int slotId) {
        this.game = game;
        this.activeSlot = slotId;
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public void applyLiveVolumeSettings() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("MyGameSettings");
        }
        float master = prefs.getFloat("master_volume", 1f);
        float sound = prefs.getFloat("sound_volume", 1f);
        float music = prefs.getFloat("menu_volume", 1f);

        if (audioManager != null) {
            audioManager.setMasterVolume(master);
            audioManager.setBaseVolume(music);
            audioManager.setSfxVolume(sound);
        }
        if (currentMusic != null) {
            currentMusic.setVolume(master * music);
        }
        if (rainAmbience != null) {
            rainAmbience.setVolume(master * music * 0.8f);
        }
    }

    public void setCurrentState(GameState currentState) {
        this.currentState = currentState;

        if (pauseOverlay == null) {
            return;
        }

        if (currentState == GameState.RUNNING) {
            Gdx.input.setInputProcessor(null);
        } else {
            Gdx.input.setInputProcessor(pauseOverlay.getStage());
        }
    }

    public Music getBackgroundMusic() {
        return currentMusic;
    }

    public void setCheckpoint(float x, float y) {
        respawnPoint.set(x, y);
        if (playerLifecycleController != null) {
            playerLifecycleController.setRespawnPoint(x, y);
        }
    }

    public void backToMainMenu() {
        game.setScreen(new MainMenuScreen(game));
        dispose();
    }

    private static final int RAIN_FRAME_COUNT = 1;
    private static final float RAIN_FRAME_DURATION = 0.08f;

    private com.badlogic.gdx.maps.MapLayer findLayerRecursive(com.badlogic.gdx.maps.MapLayers layers, String name) {
        for (com.badlogic.gdx.maps.MapLayer layer : layers) {
            if (name.equals(layer.getName())) {
                return layer;
            }
            if (layer instanceof com.badlogic.gdx.maps.MapGroupLayer) {
                com.badlogic.gdx.maps.MapLayer found = findLayerRecursive(
                    ((com.badlogic.gdx.maps.MapGroupLayer) layer).getLayers(), name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private View.RainEffect loadRainEffect(com.badlogic.gdx.maps.tiled.TiledMap map) {
        com.badlogic.gdx.maps.MapLayer layer = findLayerRecursive(map.getLayers(), "RainZone");
        if (layer == null) {
            StringBuilder names = new StringBuilder();
            for (com.badlogic.gdx.maps.MapLayer l : map.getLayers()) {
                if (names.length() > 0) names.append(", ");
                names.append("'").append(l.getName()).append("'");
            }
            Gdx.app.error("RainEffect", "Layer 'RainZone' not found in the tiled map."
                + " Create an Object Layer in Tiled with exactly this name (case sensitive)."
                + " Top-level layers found in this map: [" + names + "]");
            return null;
        }

        com.badlogic.gdx.graphics.g2d.Animation<TextureRegion> rainAnimation = null;
        float dropWidth = 0.25f;
        float dropHeight = 0.6f;
        try {
            com.badlogic.gdx.files.FileHandle rainFile = Gdx.files.internal("effects/rain/RainDrop.png");
            if (!rainFile.exists() || rainFile.length() <= 0) {
                Gdx.app.error("RainEffect", "File effects/rain/RainDrop.png not found or empty. Check the path.");
                return null;
            }
            rainTexture = new com.badlogic.gdx.graphics.Texture(rainFile);
            rainTexture.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
                com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);

            int frameW = rainTexture.getWidth() / RAIN_FRAME_COUNT;
            int frameH = rainTexture.getHeight();
            TextureRegion[][] split = TextureRegion.split(rainTexture, frameW, frameH);
            TextureRegion[] frames = new TextureRegion[RAIN_FRAME_COUNT];
            System.arraycopy(split[0], 0, frames, 0, RAIN_FRAME_COUNT);
            rainAnimation = new com.badlogic.gdx.graphics.g2d.Animation<>(RAIN_FRAME_DURATION, frames);

            dropHeight = 0.6f;
            dropWidth = dropHeight * ((float) frameW / frameH);
        } catch (Exception e) {
            Gdx.app.error("RainEffect", "Rain texture not found (effects/rain/RainDrop.png)."
                + " Rain will not render until this file is added.", e);
            return null;
        }

        for (com.badlogic.gdx.maps.MapObject obj : layer.getObjects()) {
            if (!(obj instanceof com.badlogic.gdx.maps.objects.RectangleMapObject)) {
                Gdx.app.error("RainEffect", "An object in the RainZone layer is not a rectangle ("
                    + obj.getClass().getSimpleName() + "). Use the Rectangle tool in Tiled, not Polygon/Point/Ellipse.");
                continue;
            }
            Rectangle rect = ((com.badlogic.gdx.maps.objects.RectangleMapObject) obj).getRectangle();
            Rectangle worldRect = new Rectangle(
                rect.x / TILE_SIZE, rect.y / TILE_SIZE,
                rect.width / TILE_SIZE, rect.height / TILE_SIZE
            );
            int dropCount = MathUtils.clamp(Math.round(worldRect.width * worldRect.height * 0.8f), 30, 400);
            return new View.RainEffect(worldRect, dropCount, rainAnimation, dropWidth, dropHeight);
        }
        Gdx.app.error("RainEffect", "RainZone layer found but it has no rectangle object.");
        return null;
    }

    private void triggerKnightShake() {
        triggerShake(0.12f, 0.08f);
        if (audioManager != null) {
            audioManager.playSfx("sfx/damage.wav");
        }
    }

    private void triggerShake(float duration, float power) {
        cameraShakeTime = Math.max(cameraShakeTime, duration);
        cameraShakePower = Math.max(cameraShakePower, power);
    }

    private void changeMusic(String newTrackPath) {
        if (newTrackPath == null || newTrackPath.isEmpty()) {
            return;
        }

        String resolvedPath = resolveMusicPath(newTrackPath);
        if (resolvedPath == null) {
            Gdx.app.error("GameScreen", "Music track not found: " + newTrackPath);
            return;
        }

        if (resolvedPath.equals(currentTrackPath)) {
            return;
        }

        try {
            Music nextMusic = Gdx.audio.newMusic(Gdx.files.internal(resolvedPath));

            if (prefs == null) {
                prefs = Gdx.app.getPreferences("MyGameSettings");
            }

            float masterVol = prefs.getFloat("master_volume", 1f);
            float musicVol = prefs.getFloat("menu_volume", 1f);
            nextMusic.setVolume(masterVol * musicVol);
            nextMusic.setLooping(true);

            if (currentMusic != null) {
                currentMusic.stop();
                currentMusic.dispose();
            }

            currentMusic = nextMusic;
            currentTrackPath = resolvedPath;
            currentMusic.play();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load music: " + resolvedPath, e);
        }
    }

    private String resolveMusicPath(String requestedPath) {
        String normalized = requestedPath.replace('\\', '/');

        String[] candidates = buildMusicPathCandidates(normalized);
        for (String candidate : candidates) {
            FileHandle handle = Gdx.files.internal(candidate);
            if (handle.exists() && handle.length() > 0) {
                if (!candidate.equals(normalized)) {
                    Gdx.app.log("GameScreen",
                        "Resolved music path: " + normalized + " -> " + candidate);
                }
                return candidate;
            }
        }

        return null;
    }

    private String[] buildMusicPathCandidates(String path) {
        int slash = path.lastIndexOf('/');
        String directory = slash >= 0 ? path.substring(0, slash + 1) : "";
        String fileName = slash >= 0 ? path.substring(slash + 1) : path;

        int dot = fileName.lastIndexOf('.');
        String base = dot > 0 ? fileName.substring(0, dot) : fileName;

        java.util.ArrayList<String> candidates = new java.util.ArrayList<>();

        addMusicCandidate(candidates, path);
        addMusicCandidate(candidates, directory + base + ".mp3");
        addMusicCandidate(candidates, directory + base + ".MP3");
        addMusicCandidate(candidates, directory + base + ".wav");
        addMusicCandidate(candidates, directory + base + ".WAV");

        // JAR/classpath resources are case-sensitive, unlike Windows paths.
        String[] folderVariants = {"music/", "Music/", "MUSIC/"};
        for (String folder : folderVariants) {
            if (directory.equalsIgnoreCase("music/")) {
                addMusicCandidate(candidates, folder + base + ".mp3");
                addMusicCandidate(candidates, folder + base + ".MP3");
                addMusicCandidate(candidates, folder + base + ".wav");
                addMusicCandidate(candidates, folder + base + ".WAV");
            }
        }

        addMusicCandidate(candidates, directory + base.toLowerCase() + ".mp3");
        addMusicCandidate(candidates, directory + base.toLowerCase() + ".MP3");
        addMusicCandidate(candidates, directory + base.toUpperCase() + ".mp3");
        addMusicCandidate(candidates, directory + base.toUpperCase() + ".MP3");

        return candidates.toArray(new String[0]);
    }

    private void addMusicCandidate(java.util.List<String> candidates, String candidate) {
        if (candidate != null && !candidate.isEmpty() && !candidates.contains(candidate)) {
            candidates.add(candidate);
        }
    }

    @Override
    public void show() {
        if (initialized) {
            currentState = GameState.RUNNING;
            Gdx.input.setInputProcessor(null);
            return;
        }
        initialized = true;

        prefs = Gdx.app.getPreferences("MyGameSettings");

        world = new World(new Vector2(0f, -20f), true);
        batch = new SpriteBatch();

        levelLoader = new LevelLoader();
        levelData = levelLoader.load("greenpath/greenpath.tmx", world);
        tileMapView = new TileMapView(
            levelData.getMap(),
            new OrthogonalTiledMapRenderer(levelData.getMap(), 1f / TILE_SIZE)
        );

        rainEffect = loadRainEffect(levelData.getMap());

        crackedWallController = new Controller.CrackedWallController();
        crackedWallController.load(levelData.getMap(), world);
        crackedWallAnimations = new View.CrackedWallAnimations();
        crackedWallView = new View.CrackedWallView(crackedWallAnimations);

        spawnPoint.set(levelData.getSpawnPoint());
        respawnPoint.set(spawnPoint);

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 20f, 12f);
        Rectangle arenaBounds = levelData.getArenaBounds();

        knightAnimations = new KnightAnimations();
        knightController = null;

        slashEffectAnimations = new SlashEffectAnimation();
        slashEffectAnimation = slashEffectAnimations.getSlash();
        jumpWaveAnimations = new JumpWaveAnimation();
        jumpWaveAnimation = jumpWaveAnimations.getJumpWave();
        falseKnightJumpWaveAnimations = new JumpWaveAnimation("FalseKnight/JumpWave");
        falseKnightJumpWaveAnimation = falseKnightJumpWaveAnimations.getJumpWave();
        dashEffectAnimations = new DashEffectAnimation();
        dashEffectAnimation = dashEffectAnimations.getDash();

        mosscreepAnimations = new MosscreepAnimations();
        wingedSentryAnimations = new WingedSentryAnimations();
        huskHornheadAnimations = new HuskHornheadAnimations();
        crystalGuardianAnimations = new CrystalGuardianAnimations();
        falseKnightAnimations = new FalseKnightAnimations();
        bloodSplashAnimations = new BloodSplashAnimation();
        bloodSplashAnimation = bloodSplashAnimations.getBloodSplash();
        crystalLaserTexture = new Texture("CrystalGuardian/Laser.png");

        knight = new Knight(levelLoader.createKnightBody(world, spawnPoint.x, spawnPoint.y));
        knightController = new KnightController(knight);
        charmManager = new CharmManager(knight);
        world.setContactListener(new GroundContactListener(knight, this::triggerKnightShake,
            levelData::findNearestSafeSpawn));

        world.setContactFilter(new com.badlogic.gdx.physics.box2d.ContactFilter() {
            private final java.util.Set<String> enemyTags = new java.util.HashSet<>(java.util.Arrays.asList(
                "MOSSCREEP", "WINGEDSENTRY", "HUSKHORNHEAD", "FALSE_KNIGHT", "CRYSTALGUARDIAN"
            ));

            @Override
            public boolean shouldCollide(com.badlogic.gdx.physics.box2d.Fixture fixtureA,
                                         com.badlogic.gdx.physics.box2d.Fixture fixtureB) {
                Object dataA = fixtureA.getUserData();
                Object dataB = fixtureB.getUserData();
                boolean aIsPlayer = "PLAYER".equals(dataA);
                boolean bIsPlayer = "PLAYER".equals(dataB);
                if (!aIsPlayer && !bIsPlayer) {
                    return true;
                }
                Object otherData = aIsPlayer ? dataB : dataA;
                boolean otherIsEnemy = otherData != null && enemyTags.contains(String.valueOf(otherData));
                if (otherIsEnemy && knight != null && knight.isDashing() && knight.canDashThroughEnemies()) {
                    return false;
                }
                return true;
            }
        });

        knightView = new KnightView(knight, knightAnimations);

        audioManager = new AudioManager();
        applyLiveVolumeSettings();

        enemyManager = new EnemyManager(world, mosscreepAnimations, wingedSentryAnimations, huskHornheadAnimations,
            crystalGuardianAnimations, falseKnightAnimations, falseKnightJumpWaveAnimation, crystalLaserTexture);
        enemyManager.setAudioManager(audioManager);
        enemyManager.spawnFromMap(levelData.getMap());

        combatVfxController = new CombatVfxController(
            slashEffectAnimation,
            dashEffectAnimation,
            jumpWaveAnimation,
            bloodSplashAnimation,
            enemyManager.getCrystalLasers()
        );
        enemyManager.setCombatVfxController(combatVfxController);

        vengefulSpiritEffectAnimations = new VengefulSpiritEffectAnimation();
        vengefulSpiritEffectAnimation = vengefulSpiritEffectAnimations.getBolt();
        howlingWraithsEffectAnimations = new HowlingWraithsEffectAnimation();
        howlingWraithsEffectAnimation = howlingWraithsEffectAnimations.getBurst();
        spellController = new SpellController(world, vengefulSpiritEffectAnimation, howlingWraithsEffectAnimation);

        playerLifecycleController = new PlayerLifecycleController();
        playerLifecycleController.setRespawnPoint(respawnPoint.x, respawnPoint.y);
        playerLifecycleController.setRespawnDelay(1.0f);

        Vector2 zoteSpawnPoint = levelLoader.getZoteSpawnPoint(levelData.getMap());
        zote = new Zote(levelLoader.createZoteBody(world, zoteSpawnPoint.x, zoteSpawnPoint.y));
        zoteAnimations = new ZoteAnimations();
        zoteDialogueBox = new Model.DialogueBox();
        zoteAudio = new ZoteAudio();
        zoteController = new ZoteController(zote, zoteDialogueBox, zoteAudio);
        zoteView = new ZoteView(zote, zoteAnimations);
        zoteDialogueView = new ZoteDialogueView(zote, zoteDialogueBox);

        hudScreen = new HUDScreen(knight, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        achievementManager = new AchievementManager(saveManager, activeSlot > 0 ? activeSlot : 1);
        achievementPopupView = new AchievementPopupView();
        achievementManager.addListener(achievementPopupView);
        pauseOverlay = new PauseOverlay(this);

        Gdx.input.setInputProcessor(null);

        if (levelData.getMusicPath() != null && !levelData.getMusicPath().isEmpty()) {
            changeMusic(levelData.getMusicPath());
        } else {
            changeMusic("music/room1_theme.mp3");
        }

        applyLoadedSlotIfAny();
    }

    private void applyLoadedSlotIfAny() {
        if (activeSlot <= 0) {
            return;
        }

        GameSaveData data = saveController.loadGame(activeSlot);
        if (data == null) {
            return;
        }

        knight.setMaxHealth(data.maxHealth);
        knight.setHealth(data.health);
        knight.setMaxSoul(data.maxSoul);
        knight.setSoul(data.soul);
        knight.getBody().setTransform(data.posX, data.posY, 0f);
        knight.getBody().setLinearVelocity(0f, 0f);

        respawnPoint.set(data.respawnX, data.respawnY);
        if (playerLifecycleController != null) {
            playerLifecycleController.setRespawnPoint(data.respawnX, data.respawnY);
        }

        if (charmManager != null) {
            saveController.applyLoadedCharms(charmManager, data.equippedCharmsCsv);
        }

        if (enemyManager != null) {
            if (data.falseKnightDefeated) {
                for (FalseKnight fk : enemyManager.getFalseKnights()) {
                    fk.setDead(true);
                    fk.setRemovePending(true);
                }
            }
            if (data.crystalGuardianDefeated) {
                for (CrystalGuardian cg : enemyManager.getCrystalGuardians()) {
                    cg.setDead(true);
                    cg.setRemovePending(true);
                }
            }
            if (data.wingedSentryDefeated) {
                for (WingedSentry ws : enemyManager.getWingedSentries()) {
                    ws.setState(Model.Enums.WingedSentryState.DeathLand);
                    ws.setRemovePending(true);
                }
            }

        }

        if (camera != null) {
            camera.position.set(data.posX, data.posY, 0);
            camera.update();
        }
    }

    public void saveCurrentGame() {
        if (knight == null) {
            return;
        }
        int slotToSave = activeSlot > 0 ? activeSlot : 1;

        boolean falseKnightDefeated = enemyManager != null && !enemyManager.getFalseKnights().isEmpty()
            && allDead(enemyManager.getFalseKnights());
        boolean crystalGuardianDefeated = enemyManager != null && !enemyManager.getCrystalGuardians().isEmpty()
            && allDeadCrystal(enemyManager.getCrystalGuardians());
        boolean wingedSentryDefeated = enemyManager != null && !enemyManager.getWingedSentries().isEmpty()
            && allDeadSentry(enemyManager.getWingedSentries());

        String achievementsCsv = achievementManager != null ? achievementManager.getUnlockedCsv() : "";

        saveController.saveCurrentGame(
            slotToSave, knight, charmManager,
            CURRENT_LEVEL_ID, respawnPoint.x, respawnPoint.y, "",
            falseKnightDefeated, crystalGuardianDefeated, false, wingedSentryDefeated,
            achievementsCsv
        );
        activeSlot = slotToSave;
    }

    private boolean allDead(java.util.ArrayList<FalseKnight> list) {
        for (FalseKnight fk : list) if (!fk.isDead()) return false;
        return true;
    }

    private boolean allDeathAnimationFinished(java.util.ArrayList<FalseKnight> list) {
        for (FalseKnight fk : list) {
            if (!fk.isDead() || fk.getState() != Model.Enums.FalseKnightState.DeathCorpse) return false;
        }
        return true;
    }

    private boolean allDeadCrystal(java.util.ArrayList<CrystalGuardian> list) {
        for (CrystalGuardian cg : list) if (!cg.isDead()) return false;
        return true;
    }

    private boolean allDeadSentry(java.util.ArrayList<WingedSentry> list) {
        for (WingedSentry ws : list) if (!ws.isDead()) return false;
        return true;
    }

    private void updateRunningGame(float delta) {
        if (knight == null || world == null) {
            return;
        }

        if (knight.isDead()) {
            if (enemyManager != null) {
                enemyManager.resetBossArena();
            }

            if (playerLifecycleController != null && !playerLifecycleController.isRespawnPending()) {
                playerLifecycleController.startDeathRespawn();
                deathCount++;
                if (knightView != null) {
                    knightView.resetAnimationTime();
                }
            }

            if (playerLifecycleController != null) {
                playerLifecycleController.update(delta, respawnPoint -> {
                    knight.respawn(respawnPoint);
                    if (knightView != null) {
                        knightView.resetAnimationTime();
                    }
                    if (hudScreen != null) {
                        hudScreen.playVesselIntro();
                    }
                });
            }
        } else {
            if (playerLifecycleController != null) {
                playerLifecycleController.cancelRespawn();
                playerLifecycleController.update(delta, null);
            }

            boolean zoteDialogueOpen = false;
            if (zoteController != null) {
                zoteDialogueOpen = zoteController.update(delta, knight);
            }

            if (!zoteDialogueOpen) {
                if (knight.isNoclip()) {
                    handleNoclipMovement();
                } else if (knightController != null) {
                    knightController.update(delta);
                }
            } else {
                knight.getBody().setLinearVelocity(0f, knight.getBody().getLinearVelocity().y);
            }
            knight.update(delta);

            if (audioManager != null) {
                if (knight.isAttacking() && knight.getState() != KnightState.DOWNSLASH
                    && knight.getAttackSequence() != lastNailSlashSequencePlayed) {
                    lastNailSlashSequencePlayed = knight.getAttackSequence();
                    audioManager.playSfx("sfx/nail_slash.wav");
                } else if (!knight.isAttacking()) {
                    lastNailSlashSequencePlayed = -1;
                }

                if (knight.getSoul() > lastKnightSoul) {
                    audioManager.playSfx("sfx/soul_gain.wav");
                }
                lastKnightSoul = knight.getSoul();

                boolean isCastingFocus = knight.getState() == KnightState.CAST;
                if (isCastingFocus && !wasCastingFocus) {
                    audioManager.playSfx("sfx/focus.wav");
                }
                wasCastingFocus = isCastingFocus;
            }

            if (achievementManager != null && knight.getSoul() >= 99) {
                achievementManager.unlock(AchievementManager.CUSTOM);
            }

            if (combatVfxController != null) {
                combatVfxController.handleSlashSpawn(knight);
                combatVfxController.updateSlashEffects(delta);
                combatVfxController.handleUpSlashSpawn(knight);
                combatVfxController.updateUpSlashEffects(delta);
                combatVfxController.handleDashEffectSpawn(knight);
                combatVfxController.updateDashEffects(delta);
                combatVfxController.handleJumpWaveSpawn(knight);
                combatVfxController.updateJumpWaveEffects(delta);
                combatVfxController.updateBloodSplashEffects(delta);
            }

            if (spellController != null) {
                spellController.update(delta, knight);
            }

            if (enemyManager != null) {
                enemyManager.update(delta, knight);
                enemyManager.handleDashThroughHits(knight);

                if (enemyManager.isFalseKnightRunning()) {
                    triggerShake(0.05f, 0.02f);
                }
                if (enemyManager.consumeFalseKnightSlamPulse()) {
                    triggerShake(0.18f, 0.1f);
                }

                enemyManager.handleKnightAttackHits(knight);

                if (crackedWallController != null) {
                    crackedWallController.update(delta);
                    crackedWallController.handleKnightAttackHits(knight, world);
                    crackedWallController.checkCharmPickups(knight, charmManager, achievementManager);
                }
                if (crackedWallView != null && crackedWallController != null) {
                    crackedWallView.update(crackedWallController.getWalls(), delta);
                }
                if (combatVfxController != null) {
                    enemyManager.handleKnightJumpWaveHits(knight, combatVfxController.getJumpWaveEffects());
                }
                if (spellController != null) {
                    boolean vengefulSpiritHit = enemyManager.handleVengefulSpiritHits(knight, spellController.getBolts());
                    boolean howlingWraithsHit = enemyManager.handleHowlingWraithsHits(knight, spellController.getBursts());
                    if (vengefulSpiritHit || howlingWraithsHit) {
                        triggerShake(0.12f, 0.08f);
                    }
                }
                enemyManager.cleanupDestroyedBodies();

                updateKillCount();
                checkWinCondition();
            }
        }
        world.step(1f / 60f, 6, 2);
        if (knight != null && knight.hasPendingRelocation()) {
            knight.applyPendingRelocationIfAny();
        }

        updateCamera(delta);
        if (!gameWon) {
            updateMusicByPosition();
        }
        if (audioManager != null) {
            audioManager.update(delta);
        }

        if (!gameWon) {
            elapsedSeconds += delta;
        }
    }

    private void updateKillCount() {
        if (enemyManager == null) {
            return;
        }
        for (var e : enemyManager.getMosscreeps()) {
            if (e != null && e.isDead() && countedDeadEnemies.add(e)) {
                enemiesKilledCount++;
                killedMosscreepOnce = true;
            }
        }
        for (var e : enemyManager.getWingedSentries()) {
            if (e != null && !e.isAlive() && countedDeadEnemies.add(e)) {
                enemiesKilledCount++;
                killedWingedSentryOnce = true;
            }
        }
        for (var e : enemyManager.getHuskHornheads()) {
            if (e != null && !e.isAlive() && countedDeadEnemies.add(e)) {
                enemiesKilledCount++;
                killedHuskHornheadOnce = true;
            }
        }
        for (var e : enemyManager.getCrystalGuardians()) {
            if (e != null && e.isDead() && countedDeadEnemies.add(e)) {
                enemiesKilledCount++;
                killedCrystalGuardianOnce = true;
            }
        }
        for (var e : enemyManager.getFalseKnights()) {
            if (e != null && e.isDead() && countedDeadEnemies.add(e)) enemiesKilledCount++;
        }

        if (achievementManager != null
            && killedMosscreepOnce && killedWingedSentryOnce
            && killedHuskHornheadOnce && killedCrystalGuardianOnce) {
            achievementManager.unlock(AchievementManager.TRUE_HUNTER);
        }
    }

    private void checkWinCondition() {
        if (victoryAlreadyTriggered || enemyManager == null) {
            return;
        }
        var bosses = enemyManager.getFalseKnights();
        if (!bosses.isEmpty() && allDeathAnimationFinished(bosses)) {
            gameWon = true;
            victoryAlreadyTriggered = true;
            if (achievementManager != null) {
                achievementManager.unlock(AchievementManager.FALSE_KNIGHT);
                achievementManager.unlock(AchievementManager.COMPLETION);
                if (elapsedSeconds < 20 * 60f) {
                    achievementManager.unlock(AchievementManager.SPEEDRUN);
                }
            }
            triggerEndGamePopup();
        }
    }

    private void triggerEndGamePopup() {
        if (prefs == null) {
            prefs = Gdx.app.getPreferences("MyGameSettings");
        }

        if (audioManager != null) {
            String victoryTrack = "music/victory_theme.mp3";
            com.badlogic.gdx.files.FileHandle victoryHandle = Gdx.files.internal(victoryTrack);
            if (victoryHandle.exists() && victoryHandle.length() > 0) {
                try {
                    audioManager.changeMusic(victoryTrack, false);
                } catch (Exception e) {
                    Gdx.app.error("GameScreen", "Failed to play victory music: " + victoryTrack, e);
                }
            }
        }
        boolean es = "ES".equals(prefs.getString("lang", "EN"));

        String title = es ? "¡VICTORIA!" : "VICTORY";
        String[] stats = new String[]{
            (es ? "Muertes: " : "Deaths: ") + deathCount,
            (es ? "Enemigos derrotados: " : "Enemies defeated: ") + enemiesKilledCount,
            (es ? "Tiempo total: " : "Total time: ") + formatElapsedTime(elapsedSeconds)
        };
        String restartLabel = es ? "REINICIAR" : "RESTART";
        String menuLabel = es ? "MENÚ PRINCIPAL" : "MAIN MENU";
        String countdownTemplate = es ? "El juego continuará en %d segundos" : "Game will continue in %d seconds";

        if (endGamePopup == null) {
            endGamePopup = new BossVictoryOverlay();
        }
        endGamePopup.show(title, stats, 12f, restartLabel, menuLabel, countdownTemplate, new BossVictoryOverlay.Callback() {
            @Override
            public void onRestart() {
                Gdx.input.setInputProcessor(null);
                game.setScreen(new GameScreen(game));
                dispose();
            }

            @Override
            public void onMainMenu() {
                Gdx.input.setInputProcessor(null);
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });
        Gdx.input.setInputProcessor(endGamePopup.getStage());
    }

    private String formatElapsedTime(float totalSeconds) {
        int total = Math.max(0, Math.round(totalSeconds));
        int minutes = total / 60;
        int seconds = total % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void updateCamera(float delta) {
        if (camera == null || knight == null) {
            return;
        }

        Vector2 playerPos = knight.getBody().getPosition();
        float desiredLookOffset = 0f;
        if (knight.getState() == KnightState.LOOKUP) {
            desiredLookOffset = lookCameraOffsetStrength;
        } else if (knight.getState() == KnightState.LOOKDOWN) {
            desiredLookOffset = -lookCameraOffsetStrength * 0.7f;
        }
        lookCameraOffsetY += (desiredLookOffset - lookCameraOffsetY) * 8f * delta;

        float left = camera.position.x - deadZoneWidth / 2f;
        float right = camera.position.x + deadZoneWidth / 2f;
        float bottom = camera.position.y - deadZoneHeight / 2f;
        float top = camera.position.y + deadZoneHeight / 2f;

        cameraTarget.set(camera.position.x, camera.position.y);
        float targetOffset = 0f;
        float lookOffsetY = 0f;
        if (knight.getState() == KnightState.LOOKUP) {
            targetOffset = 1f;
        } else if (knight.getState() == KnightState.LOOKDOWN) {
            targetOffset = -1f;
        } else if (knight.getState() != KnightState.LOOKUP && knight.getState() != KnightState.LOOKDOWN) {
            lookOffsetY = 0f;
            targetOffset = 0f;
        }
        lookOffsetY += (targetOffset - lookOffsetY) * 2.5f * delta;

        if (playerPos.x < left) {
            cameraTarget.x = playerPos.x + deadZoneWidth / 2f;
        }
        if (playerPos.x > right) {
            cameraTarget.x = playerPos.x - deadZoneWidth / 2f;
        }

        if (playerPos.y < bottom) {
            cameraTarget.y = playerPos.y + deadZoneHeight / 2f;
        }
        if (playerPos.y > top) {
            cameraTarget.y = playerPos.y - deadZoneHeight / 2f;
        }

        camera.position.x += (cameraTarget.x - camera.position.x) * cameraLerp * delta;
        camera.position.y += (cameraTarget.y - camera.position.y) * cameraLerp * delta + lookOffsetY;

        if (cameraShakeTime > 0f) {
            cameraShakeTime -= delta;
            camera.position.x += com.badlogic.gdx.math.MathUtils.random(-cameraShakePower, cameraShakePower);
            camera.position.y += com.badlogic.gdx.math.MathUtils.random(-cameraShakePower, cameraShakePower);
        }

        if (levelData != null && levelData.getArenaBounds() != null && levelData.getArenaBounds().width > 0f) {
            Rectangle bounds = levelData.getArenaBounds();

            if (bounds.contains(playerPos.x, playerPos.y)) {
                float halfViewportWidth = camera.viewportWidth * 0.5f;
                float halfViewportHeight = camera.viewportHeight * 0.5f;
                float minX = bounds.x + halfViewportWidth;
                float maxX = bounds.x + bounds.width - halfViewportWidth;
                float minY = bounds.y + halfViewportHeight;
                float maxY = bounds.y + bounds.height - halfViewportHeight;
                if (minX <= maxX) {
                    camera.position.x = com.badlogic.gdx.math.MathUtils.clamp(camera.position.x, minX, maxX);
                }
                if (minY <= maxY) {
                    camera.position.y = com.badlogic.gdx.math.MathUtils.clamp(camera.position.y, minY, maxY);
                }
            }
        }

        camera.update();
    }

    private void updateRainAmbience(boolean shouldPlay) {
        if (shouldPlay && !rainAmbiencePlaying) {
            if (rainAmbience == null) {
                com.badlogic.gdx.files.FileHandle handle = Gdx.files.internal(RAIN_AMBIENCE_PATH);
                if (handle.exists() && handle.length() > 0) {
                    try {
                        rainAmbience = Gdx.audio.newMusic(handle);
                        rainAmbience.setLooping(true);
                    } catch (Exception e) {
                        Gdx.app.error("RainEffect", "Failed to play rain ambience sound: " + RAIN_AMBIENCE_PATH, e);
                        rainAmbience = null;
                    }
                } else {
                    Gdx.app.error("RainEffect", "File " + RAIN_AMBIENCE_PATH + " not found or empty; only the visual rain effect will play.");
                }
            }
            if (rainAmbience != null && !rainAmbience.isPlaying()) {
                float masterVol = prefs != null ? prefs.getFloat("master_volume", 1f) : 1f;
                float musicVol = prefs != null ? prefs.getFloat("menu_volume", 1f) : 1f;
                rainAmbience.setVolume(masterVol * musicVol * 0.8f);
                rainAmbience.play();
            }
            rainAmbiencePlaying = true;
        } else if (!shouldPlay && rainAmbiencePlaying) {
            if (rainAmbience != null && rainAmbience.isPlaying()) {
                rainAmbience.stop();
            }
            rainAmbiencePlaying = false;
        }
    }

    private void updateMusicByPosition() {
        if (knight == null || levelData == null) {
            return;
        }
        Vector2 playerPos = knight.getBody().getPosition();
        String trackForHere = levelData.getMusicPathForPosition(playerPos);
        changeMusic(trackForHere);
    }

    @Override
    public void render(float delta) {
        boolean popupBlocksInput = endGamePopup != null && endGamePopup.isActive();

        if (!popupBlocksInput && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (currentState == GameState.RUNNING) {
                setCurrentState(GameState.PAUSED);
            } else if (currentState == GameState.PAUSED) {
                setCurrentState(GameState.RUNNING);
            } else if (currentState == GameState.CONTROLS) {
                setCurrentState(GameState.PAUSED);
            }
        }
        if (!popupBlocksInput && Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            game.setScreen(new CharmScreen(game, this, charmManager));
        }
        if (!popupBlocksInput && (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) &&
            Gdx.input.isKeyJustPressed(Input.Keys.B)) {

            teleportToBossRoom();
        }
        if (!popupBlocksInput) {
            handleCheatCodes();
        }

        boolean popupShowing = popupBlocksInput;
        if (popupShowing) {
            endGamePopup.update(delta);
            if (endGamePopup.consumeAutoFinished()) {
                Gdx.input.setInputProcessor(null);
                popupShowing = false;
                gameWon = false;
                updateMusicByPosition();
            }
        } else if (currentState == GameState.RUNNING) {
            updateRunningGame(delta);
        } else if (pauseOverlay != null) {
            pauseOverlay.update(delta);
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (tileMapView != null && camera != null) {
            tileMapView.setView(camera);
            tileMapView.renderBelowEntities();
        }

        if (batch != null && knightView != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            if (combatVfxController != null) {
                combatVfxController.renderSlashEffects(batch);
                combatVfxController.renderUpSlashEffects(batch);
                combatVfxController.renderDashEffects(batch);
                combatVfxController.renderJumpWaveEffects(batch);
                combatVfxController.renderBloodSplashEffects(batch);
            }

            if (spellController != null) {
                spellController.renderBolts(batch);
            }

            knightView.render(batch, delta);

            if (crackedWallView != null && crackedWallController != null) {
                crackedWallView.renderWalls(batch, crackedWallController.getWalls());
                crackedWallView.renderCharmPickups(batch, crackedWallController.getCharmPickups());
            }

            if (zoteView != null) {
                zoteView.render(batch, delta);
            }

            if (enemyManager != null) {
                enemyManager.render(batch, delta);
            }

            if (spellController != null) {
                spellController.renderBursts(batch);
            }

            batch.end();
        }

        if (tileMapView != null) {
            tileMapView.renderAboveEntities();
        }

        if (rainEffect != null && knight != null && knight.getBody() != null && batch != null) {
            Vector2 knightPos = knight.getBody().getPosition();
            boolean insideRain = rainEffect.containsPoint(knightPos.x, knightPos.y);
            if (insideRain) {
                rainEffect.update(delta);
                batch.setProjectionMatrix(camera.combined);
                batch.begin();
                rainEffect.render(batch);
                batch.end();
            }
            updateRainAmbience(insideRain);
        } else if (rainAmbiencePlaying) {
            updateRainAmbience(false);
        }

        if (crackedWallView != null && crackedWallController != null && batch != null && camera != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            crackedWallView.renderSecretRoomDarkness(batch, crackedWallController.getSecretRooms().values());
            batch.end();
        }

        if (currentState == GameState.RUNNING && zoteDialogueView != null && camera != null) {
            zoteDialogueView.render(camera);
            if (crackedWallView != null && crackedWallController != null) {
                crackedWallView.renderPickupPrompts(camera, crackedWallController.getCharmPickups());
            }
        }

        if (currentState == GameState.RUNNING && hudScreen != null) {
            hudScreen.render(batch, delta);
            if (knight != null) {
                hudScreen.renderCheatStatus(batch, knight.isGodMode(), knight.isNoclip(), knight.isEmergencyHealArmed());
            }
        }

        if (currentState != GameState.RUNNING && pauseOverlay != null) {
            pauseOverlay.draw();
        }

        if (achievementPopupView != null) {
            achievementPopupView.update(delta);
            achievementPopupView.render(batch, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        if (popupShowing) {
            endGamePopup.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = 20f;
            camera.viewportHeight = 12f;
            camera.update();
        }

        if (hudScreen != null) {
            hudScreen.resize(width, height);
        }

        if (zoteDialogueView != null) {
            zoteDialogueView.resize(width, height);
            if (crackedWallView != null) {
                crackedWallView.resize(width, height);
            }
        }

        if (pauseOverlay != null && pauseOverlay.getStage() != null) {
            pauseOverlay.getStage().getViewport().update(width, height, true);
        }

        if (endGamePopup != null) {
            endGamePopup.resize(width, height);
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (rainTexture != null) {
            rainTexture.dispose();
        }
        if (rainAmbience != null) {
            rainAmbience.stop();
            rainAmbience.dispose();
        }
        if (audioManager != null) {
            audioManager.dispose();
        }
        if (endGamePopup != null) {
            endGamePopup.dispose();
        }
        if (achievementPopupView != null) {
            achievementPopupView.dispose();
        }
        if (crackedWallAnimations != null) {
            crackedWallAnimations.dispose();
        }
        if (crackedWallView != null) {
            crackedWallView.dispose();
        }
        if (zoteAnimations != null) {
            zoteAnimations.dispose();
        }
        if (zoteAudio != null) {
            zoteAudio.dispose();
        }
        if (zoteDialogueView != null) {
            zoteDialogueView.dispose();
        }
        if (knightAnimations != null) {
            knightAnimations.dispose();
        }

        if (vengefulSpiritEffectAnimations != null) {
            vengefulSpiritEffectAnimations.dispose();
        }

        if (howlingWraithsEffectAnimations != null) {
            howlingWraithsEffectAnimations.dispose();
        }

        if (wingedSentryAnimations != null) {
            wingedSentryAnimations.dispose();
        }

        if (jumpWaveAnimations != null) {
            jumpWaveAnimations.dispose();
        }

        if (falseKnightJumpWaveAnimations != null) {
            falseKnightJumpWaveAnimations.dispose();
        }

        if (huskHornheadAnimations != null) {
            huskHornheadAnimations.dispose();
        }

        if (crystalGuardianAnimations != null) {
            crystalGuardianAnimations.dispose();
        }

        if (falseKnightAnimations != null) {
            falseKnightAnimations.dispose();
        }

        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic.dispose();
            currentMusic = null;
        }

        if (tileMapView != null) {
            tileMapView.dispose();
        }

        if (hudScreen != null) {
            hudScreen.dispose();
        }

        if (pauseOverlay != null) {
            pauseOverlay.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }

        if (world != null) {
            world.dispose();
        }

        if (crystalLaserTexture != null) {
            crystalLaserTexture.dispose();
        }
    }

    private void handleCheatCodes() {
        if (knight == null) {
            return;
        }
        boolean ctrl = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)
            || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
        if (!ctrl) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
            knight.toggleNoclip();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            knight.setHealth(knight.getMaxHealth());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            knight.setSoul(knight.getMaxSoul());
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
            if (charmManager != null) {
                charmManager.unlockAll();
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.G)) {
            knight.toggleGodMode();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {

            if (enemyManager != null) {
                enemyManager.killAllEnemiesOnScreen(knight);
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {

            knight.toggleEmergencyHeal();
        }
    }

    private void handleNoclipMovement() {
        float dx = 0f;
        float dy = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy -= 1f;
        knight.flyMove(dx, dy);
    }

    private void teleportToBossRoom() {
        if (knight == null || enemyManager == null) return;

        Rectangle arena = enemyManager.getFalseKnightArenaBounds();
        Vector2 targetPos = new Vector2(50f, 15f);

        var bosses = enemyManager.getFalseKnights();
        if (!bosses.isEmpty() && bosses.get(0) != null) {
            FalseKnight boss = bosses.get(0);
            targetPos.set(boss.getBody().getPosition());
            targetPos.x -= boss.isFacingRight() ? 6f : -6f;
            targetPos.y += 1.5f;
        } else if (arena != null && arena.width > 0) {
            targetPos.set(arena.x + arena.width * 0.3f, arena.y + arena.height * 0.6f);
        }

        knight.getBody().setTransform(targetPos, 0f);
        knight.getBody().setLinearVelocity(0f, 0f);
        knight.setState(Model.Enums.KnightState.IDLE);

        if (knightView != null) knightView.resetAnimationTime();

        if (camera != null) {
            camera.position.set(targetPos.x, targetPos.y + 3f, 0);
            camera.update();
        }
    }
}
