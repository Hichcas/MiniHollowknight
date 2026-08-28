package View.Screen;

import Model.Enums.GameState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class PauseOverlay {
    private Stage stage;
    private GameScreen gameScreen;
    private Preferences prefs;
    private BitmapFont font;
    private Texture blankTexture;

    private Table menuTable;
    private Table controlTable;
    private Table settingsTable;

    private TextButton continueButton, controlsButton, settingsButton, saveButton, quitButton;
    private TextButton controlBackBtn, settingsBackBtn;
    private Label masterLabel, soundLabel, musicLabel, brightnessLabel;
    private Slider brightnessSlider;

    public Stage getStage() {
        return stage;
    }

    public PauseOverlay(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.stage = new Stage(new ScreenViewport());
        this.prefs = Gdx.app.getPreferences("MyGameSettings");

        Pixmap pixmapBlank = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapBlank.setColor(Color.BLACK);
        pixmapBlank.fill();
        this.blankTexture = new Texture(pixmapBlank);
        pixmapBlank.dispose();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 22;
        parameter.color = Color.WHITE;
        this.font = generator.generateFont(parameter);
        generator.dispose();

        TextButton.TextButtonStyle buttonStyle = createButtonStyle();
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle titleStyle = new Label.LabelStyle(font, Color.GOLD);
        Slider.SliderStyle sliderStyle = createSliderStyle();

        menuTable = new Table();
        menuTable.setFillParent(true);

        continueButton = new TextButton("", buttonStyle);
        controlsButton = new TextButton("", buttonStyle);
        settingsButton = new TextButton("", buttonStyle);
        saveButton = new TextButton("", buttonStyle);
        quitButton = new TextButton("", buttonStyle);

        menuTable.add(continueButton).width(220).height(50).pad(10f).row();
        menuTable.add(controlsButton).width(220).height(50).pad(10f).row();
        menuTable.add(settingsButton).width(220).height(50).pad(10f).row();
        menuTable.add(saveButton).width(220).height(50).pad(10f).row();
        menuTable.add(quitButton).width(220).height(50).pad(10f).row();
        stage.addActor(menuTable);

        controlTable = new Table();
        controlTable.setFillParent(true);
        controlTable.setVisible(false);
        stage.addActor(controlTable);

        settingsTable = new Table();
        settingsTable.setFillParent(true);
        settingsTable.setVisible(false);

        masterLabel = new Label("", labelStyle);
        Slider masterSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        masterSlider.setValue(prefs.getFloat("master_volume", 1f));

        soundLabel = new Label("", labelStyle);
        Slider soundSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        soundSlider.setValue(prefs.getFloat("sound_volume", 1f));

        musicLabel = new Label("", labelStyle);
        Slider musicSlider = new Slider(0f, 1f, 0.01f, false, sliderStyle);
        musicSlider.setValue(prefs.getFloat("menu_volume", 1f));
        brightnessLabel = new Label("", labelStyle);
        brightnessSlider = new Slider(0.2f, 1.0f, 0.01f, false, sliderStyle);
        brightnessSlider.setValue(prefs.getFloat("brightness", 1f));

        settingsBackBtn = new TextButton("", buttonStyle);

        settingsTable.add(masterLabel).padRight(20f).left();
        settingsTable.add(masterSlider).width(200f).row();
        settingsTable.add(soundLabel).padRight(20f).padTop(15f).left();
        settingsTable.add(soundSlider).width(200f).padTop(15f).row();
        settingsTable.add(musicLabel).padRight(20f).padTop(15f).left();
        settingsTable.add(musicSlider).width(200f).padTop(15f).row();
        settingsTable.add(brightnessLabel).padRight(20f).padTop(15f).left();
        settingsTable.add(brightnessSlider).width(200f).padTop(15f).row();
        settingsTable.add(settingsBackBtn).colspan(2).center().padTop(30f).width(150).height(45);
        stage.addActor(settingsTable);

        masterSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("master_volume", masterSlider.getValue());
                prefs.flush();
                updateLabels();
                gameScreen.applyLiveVolumeSettings();
            }
        });
        soundSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("sound_volume", soundSlider.getValue());
                prefs.flush();
                updateLabels();
                gameScreen.applyLiveVolumeSettings();
            }
        });
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("menu_volume", musicSlider.getValue());
                prefs.flush();
                updateLabels();
                if (MainMenuScreen.getMenuMusic() != null) {
                    float masterVol = prefs.getFloat("master_volume", 1f);
                    float musicVol = musicSlider.getValue();
                    MainMenuScreen.getMenuMusic().setVolume(musicVol * masterVol);
                }
                gameScreen.applyLiveVolumeSettings();
            }
        });
        brightnessSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                prefs.putFloat("brightness", brightnessSlider.getValue());
                prefs.flush();
                updateLabels();
            }
        });

        continueButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.setCurrentState(GameState.RUNNING);
                Gdx.input.setInputProcessor(null);
            }
        });

        controlsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                buildStaticControlsMenu(titleStyle, labelStyle, buttonStyle);
                menuTable.setVisible(false);
                controlTable.setVisible(true);
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menuTable.setVisible(false);
                settingsTable.setVisible(true);
            }
        });

        settingsBackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                menuTable.setVisible(true);
                settingsTable.setVisible(false);
            }
        });

        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.saveCurrentGame();
                String lang = prefs.getString("lang", "EN");
                saveButton.setText(lang.equals("ES") ? "¡GUARDADO!" : "SAVED!");
            }
        });

        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameScreen.saveCurrentGame();
                gameScreen.backToMainMenu();
            }
        });

        updateLabels();
    }

    private void buildStaticControlsMenu(Label.LabelStyle titleStyle, Label.LabelStyle labelStyle, TextButton.TextButtonStyle buttonStyle) {
        controlTable.clearChildren();

        String lang = prefs.getString("lang", "EN");
        String titleText = lang.equals("ES") ? "CONTROLES" : "CONTROLS";
        Label controlTitle = new Label(titleText, titleStyle);
        controlTable.add(controlTitle).colspan(2).padBottom(25f).center().row();

        String[][] fixedKeys = {
            {"GODE MODE", "CTRL+G"},
            {"COMPLETE HEAL", "CTRL+H"},
            {"INF SOUL", "CTRL+S"},
            {"UNLOCK ALL CHARMS", "CTRL+U"},
            {"KILL ALL", "CTRL+K"},
            {"EMERGENCY HEAL", "CTRL+M"}
        };

        for (String[] keyPair : fixedKeys) {
            Label actionLabel = new Label(keyPair[0], labelStyle);
            Label keyLabel = new Label(keyPair[1], new Label.LabelStyle(font, Color.GOLD));

            controlTable.add(actionLabel).left().padBottom(12f).padRight(50f);
            controlTable.add(keyLabel).right().padBottom(12f).row();
        }

        String backText = lang.equals("ES") ? "VOLVER" : "BACK";
        controlBackBtn = new TextButton(backText, buttonStyle);
        controlBackBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                controlTable.setVisible(false);
                menuTable.setVisible(true);
            }
        });

        controlTable.add(controlBackBtn).colspan(2).center().padTop(25f).width(150).height(45);
    }

    private void updateLabels() {
        String lang = prefs.getString("lang", "EN");
        float master = prefs.getFloat("master_volume", 1f);
        float sound = prefs.getFloat("sound_volume", 1f);
        float music = prefs.getFloat("menu_volume", 1f);
        float bright = prefs.getFloat("brightness", 1f);

        if (lang.equals("ES")) {
            continueButton.setText("CONTINUAR");
            controlsButton.setText("CONTROLES");
            settingsButton.setText("AJUSTES");
            saveButton.setText("GUARDAR PARTIDA");
            quitButton.setText("GUARDAR Y SALIR");
            settingsBackBtn.setText("VOLVER");
            masterLabel.setText("VOLUMEN GENERAL: " + (int) (master * 100) + "%");
            soundLabel.setText("VOLUMEN EFECTOS: " + (int) (sound * 100) + "%");
            musicLabel.setText("VOLUMEN MÚSICA: " + (int) (music * 100) + "%");
            brightnessLabel.setText("BRILLO: " + (int) (bright * 100) + "%");
        } else {
            continueButton.setText("CONTINUE");
            controlsButton.setText("CONTROLS");
            settingsButton.setText("SETTINGS");
            saveButton.setText("SAVE GAME");
            quitButton.setText("SAVE & QUIT");
            settingsBackBtn.setText("BACK");
            masterLabel.setText("MASTER VOLUME: " + (int) (master * 100) + "%");
            soundLabel.setText("SOUND VOLUME: " + (int) (sound * 100) + "%");
            musicLabel.setText("MUSIC VOLUME: " + (int) (music * 100) + "%");
            brightnessLabel.setText("BRIGHTNESS: " + (int) (bright * 100) + "%");
        }
    }

    private TextButton.TextButtonStyle createButtonStyle() {
        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.fontColor = Color.WHITE;
        style.overFontColor = Color.GOLD;
        style.downFontColor = Color.GRAY;

        Pixmap pixmap = new Pixmap(200, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0.1f, 0.1f, 0.1f, 0.85f));
        pixmap.fill();
        style.up = new TextureRegionDrawable(new Texture(pixmap));
        pixmap.dispose();

        return style;
    }

    private Slider.SliderStyle createSliderStyle() {
        Pixmap pixmapBg = new Pixmap(200, 4, Pixmap.Format.RGBA8888);
        pixmapBg.setColor(Color.GRAY);
        pixmapBg.fill();
        TextureRegionDrawable sliderBg = new TextureRegionDrawable(new Texture(pixmapBg));
        pixmapBg.dispose();

        Pixmap pixmapKnob = new Pixmap(14, 14, Pixmap.Format.RGBA8888);
        pixmapKnob.setColor(Color.GOLD);
        pixmapKnob.fillCircle(7, 7, 6);
        TextureRegionDrawable sliderKnob = new TextureRegionDrawable(new Texture(pixmapKnob));
        pixmapKnob.dispose();

        return new Slider.SliderStyle(sliderBg, sliderKnob);
    }

    public void update(float delta) {
        stage.act(delta);
    }

    public void draw() {
        float currentBrightness = prefs.getFloat("brightness", 1.0f);

        if (currentBrightness < 1.0f) {
            stage.getBatch().begin();
            stage.getBatch().setColor(0, 0, 0, 1.0f - currentBrightness);
            stage.getBatch().draw(blankTexture, 0, 0, stage.getWidth(), stage.getHeight());
            stage.getBatch().setColor(Color.WHITE);
            stage.getBatch().end();
        }

        stage.draw();
    }

    public void dispose() {
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
        if (blankTexture != null) blankTexture.dispose();
    }
}
