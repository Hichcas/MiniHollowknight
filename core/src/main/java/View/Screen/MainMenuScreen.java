package View.Screen;

import Controller.SaveController;
import Model.GameSaveData;
import Model.GameSettings;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.badlogic.gdx.utils.Align;
import io.github.some_example_name.lwjgl3.Main;

import java.util.HashMap;

public class MainMenuScreen implements Screen {
    private final Main game;
    private Stage stage;
    private SpriteBatch batch;
    private BitmapFont font;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 1280f;
    private final float VIRTUAL_HEIGHT = 720f;
    private Animation<TextureRegion> backgroundAnimation;
    private float stateTime = 0f;
    private Texture[] bgTextures;
    private Cursor customCursor;
    private Texture logoEnTexture;
    private Texture logoEsTexture;
    private Image logoImage;
    private Texture arrowTexture;
    private Texture logo;
    private Texture cornerTexture;
    private Texture cornerTexture2;
    private Texture sliderBgTex;
    private Texture sliderKnobTex;
    private Texture blankTexture;
    private Texture keyBorderTex;
    private Texture pauseTex;
    private Texture resumeTex;
    private TextureRegionDrawable pauseDrawable;
    private TextureRegionDrawable resumeDrawable;
    private Table mainTable;
    private Table confirmTable;
    private Table settingsMenuTable;
    private Table audioSettingsTable;
    private Table keyboardSettingsTable;
    private Button hoveredButton = null;
    private Preferences prefs;
    private final SaveController saveController = new SaveController();

    public static Music getMenuMusic() {
        return menuMusic;
    }

    private static Music menuMusic;
    private float brightness = 1.0f;

    private TextButton startGameBtn;
    private TextButton optionsBtn;
    private TextButton GuideBtn;
    private TextButton achievementBtn;
    private TextButton quitGameBtn;

    private Label questionLabel;
    private TextButton yesBtn;
    private TextButton noBtn;

    private TextButton langBtn;
    private TextButton openAudioBtn;
    private TextButton openKeyboardBtn;
    private TextButton settingsMenuBackBtn;

    private Slider masterVolSlider;
    private Slider soundVolSlider;
    private Slider musicVolSlider;
    private Label masterVolLabel;
    private Label soundVolLabel;
    private Label musicVolLabel;
    private Slider brightnessSlider;
    private Label brightnessLabel;

    private Button musicPauseResumeBtn;
    private TextButton changeMusicBtn;
    private TextButton audioResetBtn;
    private TextButton audioBackBtn;
    private TextButton resolutionBtn;
    private TextButton fullscreenBtn;

    private boolean isRebinding = false;
    private String currentRebindAction = null;
    private HashMap<String, TextButton> rebindButtons = new HashMap<>();
    private TextButton keyboardResetBtn;
    private TextButton keyboardBackBtn;

    private boolean initialized = false;

    public MainMenuScreen(Main game) {
        this.game = game;
    }

    private void initDefaultKeys() {
        if (!prefs.contains("key_UP")) prefs.putInteger("key_UP", Input.Keys.W);
        if (!prefs.contains("key_DOWN")) prefs.putInteger("key_DOWN", Input.Keys.S);
        if (!prefs.contains("key_LEFT")) prefs.putInteger("key_LEFT", Input.Keys.A);
        if (!prefs.contains("key_RIGHT")) prefs.putInteger("key_RIGHT", Input.Keys.D);
        if (!prefs.contains("key_JUMP")) prefs.putInteger("key_JUMP", Input.Keys.SPACE);
        if (!prefs.contains("key_ATTACK")) prefs.putInteger("key_ATTACK", Input.Keys.X);
        if (!prefs.contains("key_DASH")) prefs.putInteger("key_DASH", Input.Keys.R);
        if (!prefs.contains("key_FOCUS")) prefs.putInteger("key_FOCUS", Input.Keys.F);
        if (!prefs.contains("key_MAP")) prefs.putInteger("key_MAP", Input.Keys.TAB);
        if (!prefs.contains("key_SUPER_DASH")) prefs.putInteger("key_SUPER_DASH", Input.Keys.V);
        if (!prefs.contains("key_DREAM_NAIL")) prefs.putInteger("key_DREAM_NAIL", Input.Keys.N);
        if (!prefs.contains("key_QUICK_CAST")) prefs.putInteger("key_QUICK_CAST", Input.Keys.Q);
        if (!prefs.contains("key_INVENTORY")) prefs.putInteger("key_INVENTORY", Input.Keys.I);
        prefs.flush();
    }

    private static final int[][] RESOLUTIONS = {
        {1280, 720},
        {1600, 900},
        {1920, 1080},
        {2560, 1440}
    };

    private void initDefaultDisplaySettings() {
        if (!prefs.contains("fullscreen")) {
            prefs.putBoolean("fullscreen", true);
        }
        if (!prefs.contains("res_width") || !prefs.contains("res_height")) {
            prefs.putInteger("res_width", 1280);
            prefs.putInteger("res_height", 720);
        }
        prefs.flush();
    }

    private boolean isFullscreenPref() {
        return prefs != null && prefs.getBoolean("fullscreen", true);
    }

    private String currentResolutionLabel() {
        if (prefs == null) {
            return "1280x720";
        }
        int w = prefs.getInteger("res_width", 1280);
        int h = prefs.getInteger("res_height", 720);
        return w + "x" + h;
    }

    private void applyDisplaySettings() {
        if (prefs == null) {
            return;
        }
        if (isFullscreenPref()) {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        } else {
            int w = prefs.getInteger("res_width", 1280);
            int h = prefs.getInteger("res_height", 720);
            Gdx.graphics.setWindowedMode(w, h);
        }
    }

    private void cycleResolution() {
        int w = prefs.getInteger("res_width", 1280);
        int h = prefs.getInteger("res_height", 720);
        int currentIndex = 0;
        for (int i = 0; i < RESOLUTIONS.length; i++) {
            if (RESOLUTIONS[i][0] == w && RESOLUTIONS[i][1] == h) {
                currentIndex = i;
                break;
            }
        }
        int nextIndex = (currentIndex + 1) % RESOLUTIONS.length;
        prefs.putInteger("res_width", RESOLUTIONS[nextIndex][0]);
        prefs.putInteger("res_height", RESOLUTIONS[nextIndex][1]);
        prefs.flush();

        if (!isFullscreenPref()) {
            applyDisplaySettings();
        }
    }

    private void toggleFullscreen() {
        boolean newValue = !isFullscreenPref();
        prefs.putBoolean("fullscreen", newValue);
        prefs.flush();
        applyDisplaySettings();
    }

    private void loadBackground(int bgType) {
        if (bgTextures != null) {
            for (Texture tex : bgTextures) {
                if (tex != null) tex.dispose();
            }
        }
        bgTextures = new Texture[25];
        TextureRegion[] bgFrames = new TextureRegion[25];
        for (int i = 0; i < 25; i++) {
            bgTextures[i] = new Texture(Gdx.files.internal("bg_" + bgType + "/ezgif-frame-" + (i + 1) + ".jpg"));
            bgFrames[i] = new TextureRegion(bgTextures[i]);
        }
        backgroundAnimation = new Animation<>(0.1f, bgFrames);
        GameSettings.currentBgType = bgType;
        prefs.putInteger("bg_type", bgType);
        prefs.flush();
    }

    @Override
    public void show() {
        if (initialized) {
            Gdx.input.setInputProcessor(stage);
            updateTexts();
            return;
        }
        initialized = true;

        batch = new SpriteBatch();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        prefs = Gdx.app.getPreferences("MyGameSettings");
        int savedBg = prefs.getInteger("bg_type", 0);
        loadBackground(savedBg);
        brightness = prefs.getFloat("brightness", 1.0f);
        initDefaultKeys();
        initDefaultDisplaySettings();
        applyDisplaySettings();

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (isRebinding && currentRebindAction != null) {
                    for (String action : rebindButtons.keySet()) {
                        if (!action.equals(currentRebindAction)) {
                            if (prefs.getInteger("key_" + action, -1) == keycode) {
                                prefs.putInteger("key_" + action, Input.Keys.UNKNOWN);
                            }
                        }
                    }
                    prefs.putInteger("key_" + currentRebindAction, keycode);
                    prefs.flush();
                    isRebinding = false;
                    currentRebindAction = null;
                    updateTexts();
                    return true;
                }
                return false;
            }
        });

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = 26;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose();

        Pixmap originalPixmap = new Pixmap(Gdx.files.internal("Cursor.png"));
        int potWidth = MathUtils.nextPowerOfTwo(originalPixmap.getWidth());
        int potHeight = MathUtils.nextPowerOfTwo(originalPixmap.getHeight());
        Pixmap potPixmap = new Pixmap(potWidth, potHeight, originalPixmap.getFormat());
        potPixmap.setBlending(Pixmap.Blending.None);
        potPixmap.drawPixmap(originalPixmap, 0, 0, originalPixmap.getWidth(), originalPixmap.getHeight(), 0, 0, originalPixmap.getWidth(), originalPixmap.getHeight());
        customCursor = Gdx.graphics.newCursor(potPixmap, 0, 0);
        Gdx.graphics.setCursor(customCursor);
        originalPixmap.dispose();
        potPixmap.dispose();

        arrowTexture = new Texture(Gdx.files.internal("arrow.png"));
        logoEnTexture = new Texture(Gdx.files.internal("logo_en.png"));
        logoEsTexture = new Texture(Gdx.files.internal("logo_es.png"));

        pauseTex = new Texture(Gdx.files.internal("pause.png"));
        resumeTex = new Texture(Gdx.files.internal("resume.png"));
        pauseDrawable = new TextureRegionDrawable(new TextureRegion(pauseTex));
        resumeDrawable = new TextureRegionDrawable(new TextureRegion(resumeTex));

        Pixmap pixmapBlank = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapBlank.setColor(Color.BLACK);
        pixmapBlank.fill();
        blankTexture = new Texture(pixmapBlank);
        pixmapBlank.dispose();

        Pixmap pixmapKey = new Pixmap(100, 35, Pixmap.Format.RGBA8888);
        pixmapKey.setColor(new Color(1f, 1f, 1f, 0.2f));
        pixmapKey.fill();
        pixmapKey.setColor(Color.WHITE);
        pixmapKey.drawRectangle(0, 0, 100, 35);
        keyBorderTex = new Texture(pixmapKey);
        pixmapKey.dispose();

        logoImage = new Image();
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;

        TextButton.TextButtonStyle keycapStyle = new TextButton.TextButtonStyle();
        keycapStyle.font = font;
        keycapStyle.fontColor = Color.GOLD;
        keycapStyle.overFontColor = Color.WHITE;
        keycapStyle.up = new TextureRegionDrawable(new TextureRegion(keyBorderTex));

        mainTable = new Table();
        mainTable.setTransform(true);
        mainTable.setFillParent(true);
        mainTable.center();
        mainTable.getColor().a = 0f;
        startGameBtn = new TextButton("", buttonStyle);
        optionsBtn = new TextButton("", buttonStyle);
        GuideBtn = new TextButton("", buttonStyle);
        achievementBtn = new TextButton("", buttonStyle);
        quitGameBtn = new TextButton("", buttonStyle);

        addButtonHoverListener(startGameBtn);
        addButtonHoverListener(optionsBtn);
        addButtonHoverListener(GuideBtn);
        addButtonHoverListener(achievementBtn);
        addButtonHoverListener(quitGameBtn);

        startGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showSaveSlotsMenu();
            }
        });

        achievementBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new AchievementScreen(game, MainMenuScreen.this));
            }
        });
        optionsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mainTable.setVisible(false);
                settingsMenuTable.setVisible(true);
            }
        });
        GuideBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GuideScreen(game, MainMenuScreen.this));
            }
        });
        quitGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mainTable.setVisible(false);
                confirmTable.setVisible(true);
            }
        });
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.SCARLET);
        Label copyrightLabel = new Label("© 2026 A.Aria.k Games - All Rights Reserved", labelStyle);
        mainTable.add(logoImage).center().padBottom(40f).row();
        mainTable.add(startGameBtn).center().padBottom(20f).row();
        mainTable.add(optionsBtn).center().padBottom(20f).row();
        mainTable.add(GuideBtn).center().padBottom(20f).row();
        mainTable.add(achievementBtn).center().padBottom(20f).row();
        mainTable.add(quitGameBtn).center();
        Table footerTable = new Table();
        footerTable.setFillParent(true);
        footerTable.bottom();
        footerTable.padBottom(10);
        copyrightLabel.setFontScale(0.5f);
        footerTable.add(copyrightLabel);
        stage.addActor(footerTable);

        settingsMenuTable = new Table();
        settingsMenuTable.setFillParent(true);
        settingsMenuTable.center();
        settingsMenuTable.setVisible(false);

        langBtn = new TextButton("", buttonStyle);
        openAudioBtn = new TextButton("", buttonStyle);
        openKeyboardBtn = new TextButton("", buttonStyle);
        settingsMenuBackBtn = new TextButton("", buttonStyle);
        TextButton changeBgBtn = new TextButton("BACKGROUND: " + GameSettings.currentBgType, buttonStyle);
        addButtonHoverListener(changeBgBtn);

        addButtonHoverListener(langBtn);
        addButtonHoverListener(openAudioBtn);
        addButtonHoverListener(openKeyboardBtn);
        addButtonHoverListener(settingsMenuBackBtn);
        changeBgBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (GameSettings.currentBgType + 1 == 6) {
                    GameSettings.currentBgType = 0;
                }
                GameSettings.currentBgType = (GameSettings.currentBgType + 1);
                loadBackground(GameSettings.currentBgType);
                changeBgBtn.setText("BACKGROUND: " + GameSettings.currentBgType);
            }
        });
        langBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String current = prefs.getString("lang", "EN");
                if (current.equals("EN")) {
                    prefs.putString("lang", "ES");
                } else {
                    prefs.putString("lang", "EN");
                }
                prefs.flush();
                updateTexts();
            }
        });

        openAudioBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsMenuTable.setVisible(false);
                audioSettingsTable.setVisible(true);
            }
        });

        openKeyboardBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsMenuTable.setVisible(false);
                keyboardSettingsTable.setVisible(true);
            }
        });

        settingsMenuBackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsMenuTable.setVisible(false);
                mainTable.setVisible(true);
            }
        });

        settingsMenuTable.add(langBtn).center().padBottom(25f).row();
        settingsMenuTable.add(openAudioBtn).center().padBottom(25f).row();
        settingsMenuTable.add(openKeyboardBtn).center().padBottom(25f).row();
        settingsMenuTable.add(changeBgBtn).center().padBottom(25f).row();
        settingsMenuTable.add(settingsMenuBackBtn).center().padBottom(25f).row();

        audioSettingsTable = new Table();
        audioSettingsTable.setFillParent(true);
        audioSettingsTable.center();
        audioSettingsTable.setVisible(false);

        Pixmap pixmapBg = new Pixmap(250, 4, Pixmap.Format.RGBA8888);
        pixmapBg.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
        pixmapBg.fill();
        sliderBgTex = new Texture(pixmapBg);
        TextureRegionDrawable sliderBg = new TextureRegionDrawable(sliderBgTex);
        pixmapBg.dispose();

        Pixmap pixmapKnob = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmapKnob.setColor(Color.WHITE);
        pixmapKnob.fillCircle(8, 8, 7);
        sliderKnobTex = new Texture(pixmapKnob);
        TextureRegionDrawable sliderKnob = new TextureRegionDrawable(sliderKnobTex);
        pixmapKnob.dispose();

        Slider.SliderStyle sliderStyle = new Slider.SliderStyle(sliderBg, sliderKnob);

        masterVolSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        masterVolSlider.setValue(prefs.getFloat("master_volume", 1f));

        soundVolSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        soundVolSlider.setValue(prefs.getFloat("sound_volume", 1f));

        musicVolSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        musicVolSlider.setValue(prefs.getFloat("menu_volume", 1f));

        brightnessSlider = new Slider(0.2f, 1.0f, 0.01f, false, sliderStyle);
        brightnessSlider.setValue(brightness);

        masterVolLabel = new Label("", labelStyle);
        soundVolLabel = new Label("", labelStyle);
        musicVolLabel = new Label("", labelStyle);
        brightnessLabel = new Label("", labelStyle);

        Button.ButtonStyle musicBtnStyle = new Button.ButtonStyle();
        musicPauseResumeBtn = new Button(musicBtnStyle);
        changeMusicBtn = new TextButton("", buttonStyle);
        audioResetBtn = new TextButton("", buttonStyle);
        audioBackBtn = new TextButton("", buttonStyle);
        resolutionBtn = new TextButton("", buttonStyle);
        fullscreenBtn = new TextButton("", buttonStyle);

        addButtonHoverListener(musicPauseResumeBtn);
        addButtonHoverListener(changeMusicBtn);
        addButtonHoverListener(audioResetBtn);
        addButtonHoverListener(audioBackBtn);
        addButtonHoverListener(resolutionBtn);
        addButtonHoverListener(fullscreenBtn);

        copyrightLabel.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                copyrightLabel.setColor(Color.GOLD);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                copyrightLabel.setColor(Color.GOLD);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.net.openURI("https://github.com/BetterCallAria/Hollow-Knight.git");
            }
        });

        masterVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("master_volume", masterVolSlider.getValue());
                updateTexts();
            }
        });

        soundVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("sound_volume", soundVolSlider.getValue());
                updateTexts();
            }
        });

        musicVolSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float vol = musicVolSlider.getValue();
                prefs.putFloat("menu_volume", vol);
                if (menuMusic != null) {
                    menuMusic.setVolume(vol);
                }
                updateTexts();
            }
        });

        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                brightness = brightnessSlider.getValue();
                prefs.putFloat("brightness", brightness);
                updateTexts();
            }
        });

        musicPauseResumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (menuMusic != null) {
                    if (menuMusic.isPlaying()) {
                        menuMusic.pause();
                    } else {
                        menuMusic.play();
                    }
                    updateTexts();
                }
            }
        });

        changeMusicBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String currentMusic = prefs.getString("menu_music", "music/menu_default.mp3");
                String nextMusic = currentMusic.contains("menu_default.mp3") ? "music/menu_alt.mp3" : "music/menu_default.mp3";

                if (menuMusic != null) {
                    menuMusic.stop();
                    menuMusic.dispose();
                }

                prefs.putString("menu_music", nextMusic);
                prefs.flush();

                if (Gdx.files.internal(nextMusic).exists()) {
                    menuMusic = Gdx.audio.newMusic(Gdx.files.internal(nextMusic));
                    menuMusic.setLooping(true);
                    menuMusic.setVolume(prefs.getFloat("menu_volume", 1f));
                    menuMusic.play();
                }
                updateTexts();
            }
        });

        resolutionBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cycleResolution();
                updateTexts();
            }
        });

        fullscreenBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleFullscreen();
                updateTexts();
            }
        });

        audioResetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putFloat("master_volume", 1.0f);
                prefs.putFloat("sound_volume", 1.0f);
                prefs.putFloat("menu_volume", 1.0f);
                prefs.putFloat("brightness", 1.0f);
                prefs.putString("menu_music", "music/menu_default.mp3");
                prefs.flush();

                masterVolSlider.setValue(1.0f);
                soundVolSlider.setValue(1.0f);
                musicVolSlider.setValue(1.0f);
                brightnessSlider.setValue(1.0f);
                brightness = 1.0f;

                if (menuMusic != null) {
                    menuMusic.stop();
                    menuMusic.dispose();
                }

                if (Gdx.files.internal("music/menu_default.mp3").exists()) {
                    menuMusic = Gdx.audio.newMusic(Gdx.files.internal("music/menu_default.mp3"));
                    menuMusic.setLooping(true);
                    menuMusic.setVolume(1.0f);
                    menuMusic.play();
                }
                updateTexts();
            }
        });

        audioBackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.flush();
                audioSettingsTable.setVisible(false);
                settingsMenuTable.setVisible(true);
            }
        });

        audioSettingsTable.add(masterVolLabel).left().padRight(30f);
        audioSettingsTable.add(masterVolSlider).width(300f).row();
        audioSettingsTable.add(soundVolLabel).left().padRight(30f).padTop(15f);
        audioSettingsTable.add(soundVolSlider).width(300f).padTop(15f).row();
        audioSettingsTable.add(musicVolLabel).left().padRight(30f).padTop(15f);
        audioSettingsTable.add(musicVolSlider).width(300f).padTop(15f).row();
        audioSettingsTable.add(brightnessLabel).left().padRight(30f).padTop(15f);
        audioSettingsTable.add(brightnessSlider).width(300f).padTop(15f).row();
        audioSettingsTable.add(changeMusicBtn).colspan(2).center().padTop(20f).row();
        audioSettingsTable.add(musicPauseResumeBtn).colspan(2).center().padTop(15f).size(64f, 64f).row();
        audioSettingsTable.add(resolutionBtn).colspan(2).center().padTop(20f).row();
        audioSettingsTable.add(fullscreenBtn).colspan(2).center().padTop(10f).row();
        audioSettingsTable.add(audioResetBtn).colspan(2).center().padTop(15f).row();
        audioSettingsTable.add(audioBackBtn).colspan(2).center().padTop(15f);

        keyboardSettingsTable = new Table();
        keyboardSettingsTable.setFillParent(true);
        keyboardSettingsTable.center();
        keyboardSettingsTable.setVisible(false);

        String[] leftActions = {"UP", "DOWN", "JUMP", "ATTACK", "DASH", "FOCUS"};
        String[] rightActions = {"LEFT", "RIGHT", "MAP", "SUPER_DASH", "DREAM_NAIL", "QUICK_CAST"};

        Table leftColumnTable = new Table();
        for (String action : leftActions) {
            Label actionLabel = new Label(action.replace("_", " "), labelStyle);
            TextButton btn = new TextButton("", keycapStyle);
            registerRebindListener(action, btn);
            rebindButtons.put(action, btn);
            leftColumnTable.add(actionLabel).left().padRight(20f).width(150f);
            leftColumnTable.add(btn).size(120f, 38f).padBottom(12f).row();
        }

        Table rightColumnTable = new Table();
        for (String action : rightActions) {
            Label actionLabel = new Label(action.replace("_", " "), labelStyle);
            TextButton btn = new TextButton("", keycapStyle);
            registerRebindListener(action, btn);
            rebindButtons.put(action, btn);
            rightColumnTable.add(actionLabel).left().padRight(20f).width(150f);
            rightColumnTable.add(btn).size(120f, 38f).padBottom(12f).row();
        }

        Table centerBottomTable = new Table();
        Label invLabel = new Label("INVENTORY", labelStyle);
        TextButton invBtn = new TextButton("", keycapStyle);
        registerRebindListener("INVENTORY", invBtn);
        rebindButtons.put("INVENTORY", invBtn);
        centerBottomTable.add(invLabel).left().padRight(20f);
        centerBottomTable.add(invBtn).size(120f, 38f);

        keyboardResetBtn = new TextButton("", buttonStyle);
        keyboardBackBtn = new TextButton("", buttonStyle);

        addButtonHoverListener(keyboardResetBtn);
        addButtonHoverListener(keyboardBackBtn);

        keyboardResetBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.putInteger("key_UP", Input.Keys.W);
                prefs.putInteger("key_DOWN", Input.Keys.S);
                prefs.putInteger("key_LEFT", Input.Keys.A);
                prefs.putInteger("key_RIGHT", Input.Keys.D);
                prefs.putInteger("key_JUMP", Input.Keys.SPACE);
                prefs.putInteger("key_ATTACK", Input.Keys.X);
                prefs.putInteger("key_DASH", Input.Keys.R);
                prefs.putInteger("key_FOCUS", Input.Keys.F);
                prefs.putInteger("key_MAP", Input.Keys.TAB);
                prefs.putInteger("key_SUPER_DASH", Input.Keys.V);
                prefs.putInteger("key_DREAM_NAIL", Input.Keys.N);
                prefs.putInteger("key_QUICK_CAST", Input.Keys.Q);
                prefs.putInteger("key_INVENTORY", Input.Keys.I);
                prefs.flush();
                updateTexts();
            }
        });

        keyboardBackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                prefs.flush();
                keyboardSettingsTable.setVisible(false);
                settingsMenuTable.setVisible(true);
            }
        });

        Table splitTable = new Table();
        splitTable.add(leftColumnTable).padRight(60f);
        splitTable.add(rightColumnTable);

        keyboardSettingsTable.add(splitTable).center().row();
        keyboardSettingsTable.add(centerBottomTable).center().padTop(10f).padBottom(25f).row();
        keyboardSettingsTable.add(keyboardResetBtn).center().padBottom(15f).row();
        keyboardSettingsTable.add(keyboardBackBtn).center();

        if (menuMusic == null) {
            String musicPath = prefs.getString("menu_music", "music/menu_default.mp3");
            if (Gdx.files.internal(musicPath).exists()) {
                menuMusic = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
                menuMusic.setLooping(true);
                menuMusic.setVolume(prefs.getFloat("menu_volume", 1f));
            }
        } else {
            menuMusic.setVolume(prefs.getFloat("menu_volume", 1f));
        }

        confirmTable = new Table();
        confirmTable.setFillParent(true);
        confirmTable.center();
        confirmTable.setVisible(false);

        questionLabel = new Label("", labelStyle);
        yesBtn = new TextButton("", buttonStyle);
        noBtn = new TextButton("", buttonStyle);

        addButtonHoverListener(yesBtn);
        addButtonHoverListener(noBtn);

        yesBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        noBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmTable.setVisible(false);
                mainTable.setVisible(true);
            }
        });

        confirmTable.add(questionLabel).center().padBottom(40f).row();
        confirmTable.add(yesBtn).center().padBottom(20f).row();
        confirmTable.add(noBtn).center();

        stage.addActor(mainTable);
        stage.addActor(settingsMenuTable);
        stage.addActor(audioSettingsTable);
        stage.addActor(keyboardSettingsTable);
        stage.addActor(confirmTable);

        updateTexts();

        cornerTexture = new Texture(Gdx.files.internal("corner_decoration.jpg"));
        cornerTexture2 = new Texture(Gdx.files.internal("corner_decoration2.png"));
        Image cornerImage = new Image(cornerTexture);
        Image cornerImage2 = new Image(cornerTexture2);
        cornerImage.setPosition(0, 0);
        cornerImage2.setPosition(VIRTUAL_WIDTH - cornerImage2.getWidth(), 0);
        stage.addActor(cornerImage);
        stage.addActor(cornerImage2);

        logo = new Texture(Gdx.files.internal("splash_team.png"));
        Image teamSplashImage = new Image(logo);
        teamSplashImage.setSize(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        stage.addActor(teamSplashImage);

        teamSplashImage.addAction(Actions.sequence(
            Actions.delay(2.0f),
            Actions.fadeOut(1.2f),
            Actions.run(() -> {
                mainTable.addAction(Actions.fadeIn(1.0f));
                if (menuMusic != null) {
                    menuMusic.play();
                }
            }),
            Actions.removeActor()
        ));
    }

    private void registerRebindListener(final String action, final TextButton button) {
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isRebinding) {
                    isRebinding = true;
                    currentRebindAction = action;
                    button.setText("...");
                }
            }
        });
    }

    private void updateTexts() {
        String lang = prefs.getString("lang", "EN");
        float masterVol = prefs.getFloat("master_volume", 1f);
        float soundVol = prefs.getFloat("sound_volume", 1f);
        float musicVol = prefs.getFloat("menu_volume", 1f);
        boolean isMusicPlaying = menuMusic != null && menuMusic.isPlaying();

        if (musicPauseResumeBtn != null) {
            musicPauseResumeBtn.getStyle().up = isMusicPlaying ? pauseDrawable : resumeDrawable;
        }

        String currentMusic = prefs.getString("menu_music", "music/menu_default.mp3");
        String trackName = currentMusic.contains("menu_alt.mp3") ? "Track 2" : "Track 1";
        String trackNameEs = currentMusic.contains("menu_alt.mp3") ? "Pista 2" : "Pista 1";

        if (lang.equals("ES")) {
            logoImage.setDrawable(new TextureRegionDrawable(logoEsTexture));
            startGameBtn.setText("JUGAR");
            optionsBtn.setText("OPCIONES");
            GuideBtn.setText("GUÍA");
            achievementBtn.setText("LOGROS");
            quitGameBtn.setText("SALIR DEL JUEGO");
            questionLabel.setText("¿ESTÁS SEGURO DE QUE QUIERES SALIR?");
            yesBtn.setText("SÍ");
            noBtn.setText("NO");
            langBtn.setText("IDIOMA: ES");
            openAudioBtn.setText("AUDIO");
            openKeyboardBtn.setText("CONTROLES");
            settingsMenuBackBtn.setText("VOLVER");
            masterVolLabel.setText("VOLUMEN GENERAL: " + (int) (masterVol * 100));
            soundVolLabel.setText("VOLUMEN EFECTOS: " + (int) (soundVol * 100));
            musicVolLabel.setText("VOLUMEN MÚSICA: " + (int) (musicVol * 100));
            brightnessLabel.setText("BRILLO: " + (int) (brightness * 100));
            if (changeMusicBtn != null) changeMusicBtn.setText("CAMBIAR MÚSICA: " + trackNameEs);
            if (resolutionBtn != null) resolutionBtn.setText("RESOLUCIÓN: " + currentResolutionLabel());
            if (fullscreenBtn != null) fullscreenBtn.setText("PANTALLA COMPLETA: " + (isFullscreenPref() ? "SÍ" : "NO"));
            audioResetBtn.setText("RESTABLECER VALORES");
            audioBackBtn.setText("VOLVER");
            keyboardResetBtn.setText("RESTABLECER TECLAS");
            keyboardBackBtn.setText("VOLVER");
        } else {
            logoImage.setDrawable(new TextureRegionDrawable(logoEnTexture));
            startGameBtn.setText("START GAME");
            optionsBtn.setText("OPTIONS");
            GuideBtn.setText("GUIDE");
            achievementBtn.setText("ACHIEVEMENTS");
            quitGameBtn.setText("QUIT GAME");
            questionLabel.setText("ARE YOU SURE YOU WANT TO QUIT?");
            yesBtn.setText("YES");
            noBtn.setText("NO");
            langBtn.setText("LANGUAGE: EN");
            openAudioBtn.setText("AUDIO");
            openKeyboardBtn.setText("KEYBOARD");
            settingsMenuBackBtn.setText("BACK");
            masterVolLabel.setText("MASTER VOLUME: " + (int) (masterVol * 100));
            soundVolLabel.setText("SOUND VOLUME: " + (int) (soundVol * 100));
            musicVolLabel.setText("MUSIC VOLUME: " + (int) (musicVol * 100));
            brightnessLabel.setText("BRIGHTNESS: " + (int) (brightness * 100));
            if (changeMusicBtn != null) changeMusicBtn.setText("MUSIC TRACK: " + trackName);
            if (resolutionBtn != null) resolutionBtn.setText("RESOLUTION: " + currentResolutionLabel());
            if (fullscreenBtn != null) fullscreenBtn.setText("FULLSCREEN: " + (isFullscreenPref() ? "ON" : "OFF"));
            audioResetBtn.setText("RESET DEFAULTS");
            audioBackBtn.setText("BACK");
            keyboardResetBtn.setText("RESET DEFAULTS");
            keyboardBackBtn.setText("BACK");
        }

        for (String action : rebindButtons.keySet()) {
            if (isRebinding && action.equals(currentRebindAction)) {
                continue;
            }
            int code = prefs.getInteger("key_" + action, Input.Keys.UNKNOWN);
            if (code == Input.Keys.UNKNOWN || code == 0) {
                rebindButtons.get(action).setText("...");
            } else {
                rebindButtons.get(action).setText(Input.Keys.toString(code));
            }
        }
    }

    private static final float BUTTON_HOVER_SCALE = 1.12f;
    private static final float BUTTON_HOVER_DURATION = 0.12f;

    private void addButtonHoverListener(Button button) {
        button.setTransform(true);
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) {
                    hoveredButton = (Button) event.getListenerActor();
                    button.setOrigin(Align.center);
                    button.clearActions();
                    button.addAction(Actions.scaleTo(BUTTON_HOVER_SCALE, BUTTON_HOVER_SCALE,
                        BUTTON_HOVER_DURATION, Interpolation.pow2Out));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                if (pointer == -1) {
                    if (hoveredButton == event.getListenerActor()) {
                        hoveredButton = null;
                    }
                    button.setOrigin(Align.center);
                    button.clearActions();
                    button.addAction(Actions.scaleTo(1f, 1f, BUTTON_HOVER_DURATION, Interpolation.pow2Out));
                }
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;
        TextureRegion currentFrame = backgroundAnimation.getKeyFrame(stateTime, true);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(currentFrame, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.end();

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();

        boolean isMenuVisible = mainTable.getColor().a >= 0.8f && mainTable.isVisible();
        boolean isSubVisible = settingsMenuTable.isVisible() || audioSettingsTable.isVisible() ||
            keyboardSettingsTable.isVisible() || (saveSlotsTable != null && saveSlotsTable.isVisible());
        if (hoveredButton != null && hoveredButton.isVisible() && (isMenuVisible || isSubVisible)) {
            Vector2 btnPos = hoveredButton.localToStageCoordinates(new Vector2(0, 0));

            batch.begin();
            if (isMenuVisible) {
                batch.setColor(1, 1, 1, mainTable.getColor().a);
            } else {
                batch.setColor(Color.WHITE);
            }

            float arrowY = btnPos.y + (hoveredButton.getHeight() - arrowTexture.getHeight()) / 2f;
            batch.draw(arrowTexture, btnPos.x - arrowTexture.getWidth() - 20f, arrowY, arrowTexture.getWidth(), arrowTexture.getHeight());
            batch.draw(arrowTexture, btnPos.x + hoveredButton.getWidth() + 75f, arrowY, -arrowTexture.getWidth(), arrowTexture.getHeight());

            batch.setColor(Color.WHITE);
            batch.end();
        }

        if (brightness < 1.0f) {
            batch.setProjectionMatrix(viewport.getCamera().combined);
            batch.begin();
            batch.setColor(0, 0, 0, 1.0f - brightness);
            batch.draw(blankTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            batch.setColor(Color.WHITE);
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        if (prefs != null) {
            prefs.flush();
        }

    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        if (font != null) font.dispose();
        if (menuMusic != null) menuMusic.dispose();

        if (logoEnTexture != null) logoEnTexture.dispose();
        if (logoEsTexture != null) logoEsTexture.dispose();
        if (arrowTexture != null) arrowTexture.dispose();
        if (logo != null) logo.dispose();
        if (customCursor != null) customCursor.dispose();
        if (cornerTexture != null) cornerTexture.dispose();
        if (cornerTexture2 != null) cornerTexture2.dispose();
        if (sliderBgTex != null) sliderBgTex.dispose();
        if (sliderKnobTex != null) sliderKnobTex.dispose();
        if (blankTexture != null) blankTexture.dispose();
        if (keyBorderTex != null) keyBorderTex.dispose();

        if (pauseTex != null) pauseTex.dispose();
        if (resumeTex != null) resumeTex.dispose();

        if (bgTextures != null) {
            for (Texture tex : bgTextures) {
                if (tex != null) tex.dispose();
            }
        }
    }

    private void startGameInSlot(int slotNumber) {

        stopMenuMusic();
        game.setScreen(new GameScreen(game, slotNumber));
    }

    public static void stopMenuMusic() {
        if (menuMusic != null) {
            menuMusic.stop();
            menuMusic.dispose();
            menuMusic = null;
        }
    }

    private Table saveSlotsTable;

    private void showSaveSlotsMenu() {
        mainTable.setVisible(false);

        if (saveSlotsTable != null) {
            saveSlotsTable.remove();
        }

        saveSlotsTable = new Table();
        saveSlotsTable.setFillParent(true);
        saveSlotsTable.center();

        String lang = prefs.getString("lang", "EN");

        String titleText = lang.equals("ES") ? "SELECCIONAR RANURA" : "SELECT SAVE SLOT";
        Label titleLabel = new Label(titleText, new Label.LabelStyle(font, Color.GOLD));
        saveSlotsTable.add(titleLabel).padBottom(30f).row();

        for (int i = 1; i <= 4; i++) {
            final int slotNum = i;
            GameSaveData saveData = null;
            try {
                saveData = saveController.loadGame(slotNum);
            } catch (RuntimeException ex) {

                Gdx.app.error("MainMenuScreen", "Failed to read save slot " + slotNum, ex);
            }
            boolean isOccupied = saveData != null;

            String buttonText;
            if (isOccupied) {
                buttonText = (lang.equals("ES") ? "Ranura " : "Slot ") + slotNum
                    + " - " + saveData.currentLevelId
                    + " (HP " + saveData.health + "/" + saveData.maxHealth + ")";
            } else {
                buttonText = (lang.equals("ES") ? "Ranura Vacía " : "Empty Slot ") + slotNum + " - (New Game)";
            }

            TextButton.TextButtonStyle slotBtnStyle = new TextButton.TextButtonStyle();
            slotBtnStyle.font = font;
            slotBtnStyle.fontColor = isOccupied ? Color.WHITE : Color.LIGHT_GRAY;
            slotBtnStyle.overFontColor = Color.GOLD;

            TextButton slotBtn = new TextButton(buttonText, slotBtnStyle);
            addButtonHoverListener(slotBtn);

            slotBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    startGameInSlot(slotNum);
                }
            });

            saveSlotsTable.add(slotBtn).padBottom(isOccupied ? 5f : 15f).row();

            if (isOccupied) {
                String deleteText = lang.equals("ES") ? "Eliminar" : "Delete";
                TextButton.TextButtonStyle deleteBtnStyle = new TextButton.TextButtonStyle();
                deleteBtnStyle.font = font;
                deleteBtnStyle.fontColor = Color.SCARLET;
                deleteBtnStyle.overFontColor = Color.RED;
                TextButton deleteBtn = new TextButton(deleteText, deleteBtnStyle);
                addButtonHoverListener(deleteBtn);
                deleteBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        saveController.deleteSlot(slotNum);
                        showSaveSlotsMenu();
                    }
                });
                saveSlotsTable.add(deleteBtn).padBottom(15f).row();
            }
        }

        String backText = lang.equals("ES") ? "VOLVER" : "BACK";
        TextButton.TextButtonStyle backBtnStyle = new TextButton.TextButtonStyle();
        backBtnStyle.font = font;
        backBtnStyle.fontColor = Color.GRAY;
        backBtnStyle.overFontColor = Color.WHITE;

        TextButton backBtn = new TextButton(backText, backBtnStyle);
        addButtonHoverListener(backBtn);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                saveSlotsTable.setVisible(false);
                mainTable.setVisible(true);
            }
        });

        saveSlotsTable.add(backBtn).padTop(20f);
        stage.addActor(saveSlotsTable);
    }

}
