package View.Screen;

import Controller.SaveManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.lwjgl3.Main;

public class AchievementScreen implements Screen {
    private final Main game;
    private final Screen mainMenuScreen;
    private final SaveManager saveManager = new SaveManager();
    private Stage stage;
    private SpriteBatch batch;

    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont descFont;

    private final float VIRTUAL_WIDTH = 1280f;
    private final float VIRTUAL_HEIGHT = 720f;
    private Viewport viewport;

    private Animation<TextureRegion> backgroundAnimation;
    private float stateTime = 0f;
    private Texture[] bgTextures;
    private Texture arrowTexture;
    private Texture cornerTexture;
    private Texture blankTexture;
    private Texture itemBorderTexture;

    private Texture iconCompletion;
    private Texture iconSpeedrun;
    private Texture iconTrueHunter;
    private Texture iconFalseKnight;
    private Texture iconCustom;
    private Texture iconLock;

    private Table containerTable;
    private Button hoveredButton = null;

    private Preferences prefs;
    private float brightness = 1.0f;
    private String lang;

    private Label screenTitleLabel;
    private TextButton backBtn;
    private static Texture[] cachedBgTextures;
    private Texture scrollTrackTexture;
    private Texture scrollKnobTexture;

    public AchievementScreen(Main game, Screen mainMenuScreen) {
        this.game = game;
        this.mainMenuScreen = mainMenuScreen;
    }

    private Texture loadTextureSafely(String path, Color fallbackColor, boolean isLock) {
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        } else {
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            if (isLock) {
                pixmap.setColor(Color.RED);
                pixmap.fill();
                pixmap.setColor(Color.BLACK);
                pixmap.drawRectangle(16, 16, 32, 32);
            } else {
                pixmap.setColor(fallbackColor);
                pixmap.fill();
            }
            Texture tex = new Texture(pixmap);
            pixmap.dispose();
            return tex;
        }
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        prefs = Gdx.app.getPreferences("MyGameSettings");
        brightness = prefs.getFloat("brightness", 1.0f);
        lang = prefs.getString("lang", "EN");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = 46;
        parameter.color = Color.GOLD;
        titleFont = generator.generateFont(parameter);

        parameter.size = 24;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);

        parameter.size = 18;
        parameter.color = Color.LIGHT_GRAY;
        descFont = generator.generateFont(parameter);
        generator.dispose();

        if (cachedBgTextures == null) {
            cachedBgTextures = new Texture[25];
            for (int i = 0; i < 25; i++) {
                cachedBgTextures[i] = new Texture(Gdx.files.internal("bg_3" + "/ezgif-frame-" + (i + 1) + ".jpg"));
            }
        }
        bgTextures = cachedBgTextures;

        TextureRegion[] bgFrames = new TextureRegion[25];
        for (int i = 0; i < 25; i++) {
            bgFrames[i] = new TextureRegion(bgTextures[i]);
        }
        backgroundAnimation = new Animation<>(0.1f, bgFrames);
        arrowTexture = new Texture(Gdx.files.internal("arrow.png"));
        cornerTexture = new Texture(Gdx.files.internal("corner_decoration.jpg"));

        scrollTrackTexture = loadTextureSafely("ui/scroll_track.png", Color.DARK_GRAY, false);
        scrollKnobTexture = loadTextureSafely("ui/scroll_knob.png", Color.LIGHT_GRAY, false);
        itemBorderTexture = loadTextureSafely("achievements/achievement_border.png",
            new Color(0.2f, 0.2f, 0.2f, 0.8f), false);
        iconCompletion = loadTextureSafely("achievements/completion.png", Color.GOLD, false);
        iconSpeedrun = loadTextureSafely("achievements/speedrun.png", Color.CYAN, false);
        iconTrueHunter = loadTextureSafely("achievements/true_hunter.png", Color.RED, false);
        iconFalseKnight = loadTextureSafely("achievements/false_knight.png", Color.PURPLE, false);
        iconCustom = loadTextureSafely("achievements/custom.png", Color.GREEN, false);
        iconLock = loadTextureSafely("achievements/locked.png", Color.GRAY, true);

        Pixmap pixmapBlank = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapBlank.setColor(Color.BLACK);
        pixmapBlank.fill();
        blankTexture = new Texture(pixmapBlank);
        pixmapBlank.dispose();

        containerTable = new Table();
        containerTable.setFillParent(true);
        containerTable.center().pad(40f);

        screenTitleLabel = new Label("", new Label.LabelStyle(titleFont, Color.GOLD));
        screenTitleLabel.setAlignment(Align.center);
        containerTable.add(screenTitleLabel).padBottom(25f).row();

        Table scrollContentTable = new Table();
        scrollContentTable.top().center();

        if (lang.equals("ES")) {
            addAchievementBox(scrollContentTable, "ach_completion", iconCompletion,
                "COMPLETACIÓN", "Termina el juego con éxito y presencia el destino del caballero.");
            addAchievementBox(scrollContentTable, "ach_speedrun", iconSpeedrun,
                "SPEEDRUN", "Completa el juego en un tiempo récord de menos de 20 minutos.");
            addAchievementBox(scrollContentTable, "ach_true_hunter", iconTrueHunter,
                "CAZADOR VERDADERO", "Derrota a todas las criaturas del diario y demuestra tu fuerza.");
            addAchievementBox(scrollContentTable, "ach_false_knight", iconFalseKnight,
                "DERROTAR A FALSE KNIGHT", "Vence al imponente False Knight en los encrucijadas.");
            addAchievementBox(scrollContentTable, "ach_custom", iconCustom,
                "CABALLERO DE SHARIF", "Lleva tu medidor de alma al límite absoluto de 99 unidades.");
        } else {
            addAchievementBox(scrollContentTable, "ach_completion", iconCompletion,
                "COMPLETION", "Successfully finish and complete the game's main journey.");
            addAchievementBox(scrollContentTable, "ach_speedrun", iconSpeedrun,
                "SPEEDRUN", "Conquer and complete the entire game in under 20 minutes.");
            addAchievementBox(scrollContentTable, "ach_true_hunter", iconTrueHunter,
                "TRUE HUNTER", "Slay every single unique type of enemy and record them.");
            addAchievementBox(scrollContentTable, "ach_false_knight", iconFalseKnight,
                "DEFEAT FALSE KNIGHT", "Defeat the mighty False Knight boss in Green Path.");
            addAchievementBox(scrollContentTable, "ach_custom", iconCustom,
                "SHARIFIAN KNIGHT", "Max out your soul vessel capacity to the absolute limit of 99.");
        }

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        TextureRegionDrawable trackDrawable = new TextureRegionDrawable(new TextureRegion(scrollTrackTexture));
        float scrollBarWidth = 0.1f;
        trackDrawable.setMinWidth(scrollBarWidth);
        scrollStyle.vScroll = trackDrawable;
        scrollStyle.vScrollKnob = new TextureRegionDrawable(new TextureRegion(scrollKnobTexture));
        ScrollPane scrollPane = new ScrollPane(scrollContentTable, scrollStyle);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setVariableSizeKnobs(false);

        containerTable.add(scrollPane).expand().fill().padBottom(15f).row();

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;

        backBtn = new TextButton("", buttonStyle);
        addButtonHoverListener(backBtn);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(mainMenuScreen);
            }
        });
        containerTable.add(backBtn).center().padTop(5f);

        stage.addActor(containerTable);

        Image cornerImage = new Image(cornerTexture);
        cornerImage.setPosition(0, 0);
        stage.addActor(cornerImage);

        updateTexts();
    }

    private void addAchievementBox(Table mainTable, String key, Texture icon, String title, String desc) {
        boolean isUnlocked = saveManager.isAchievementUnlockedInAnySlot(key);
        Table rowCard = new Table();
        rowCard.setBackground(new TextureRegionDrawable(new TextureRegion(itemBorderTexture)));
        rowCard.pad(15f);

        Image iconImage = new Image(isUnlocked ? icon : iconLock);
        if (!isUnlocked) {
            iconImage.setColor(0.3f, 0.3f, 0.3f, 0.6f);
        } else {
            iconImage.setColor(Color.WHITE);
        }

        Table textTable = new Table();
        textTable.left();

        Color titleColor = isUnlocked ? Color.GOLD : Color.DARK_GRAY;
        Color descColor = isUnlocked ? new Color(0.92f, 0.80f, 0.48f, 1f) : Color.GRAY;

        Label.LabelStyle nameStyle = new Label.LabelStyle(font, titleColor);
        Label.LabelStyle descStyle = new Label.LabelStyle(descFont, descColor);

        Label nameLabel = new Label(title, nameStyle);
        Label descriptionLabel = new Label(desc, descStyle);
        descriptionLabel.setWrap(true);

        textTable.add(nameLabel).left().row();
        textTable.add(descriptionLabel).left().width(800f).padTop(6f);

        rowCard.add(iconImage).size(64f, 64f).padRight(20f).left();
        rowCard.add(textTable).expandX().fillX().left();

        if (!isUnlocked) {
            rowCard.setColor(0.8f, 0.8f, 0.8f, 0.9f);
        } else {
            rowCard.setColor(Color.WHITE);
        }

        mainTable.add(rowCard).width(1000f).padBottom(15f).center().row();
    }

    private void updateTexts() {
        if (lang.equals("ES")) {
            screenTitleLabel.setText("LOGROS");
            backBtn.setText("VOLVER");
        } else {
            screenTitleLabel.setText("ACHIEVEMENTS");
            backBtn.setText("BACK");
        }
    }

    private void addButtonHoverListener(Button button) {
        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                if (pointer == -1) hoveredButton = (Button) event.getListenerActor();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                if (pointer == -1 && hoveredButton == event.getListenerActor()) hoveredButton = null;
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

        if (hoveredButton != null && hoveredButton.isVisible() && containerTable.isVisible()) {
            Vector2 btnPos = hoveredButton.localToStageCoordinates(new Vector2(0, 0));
            batch.begin();
            float arrowY = btnPos.y + (hoveredButton.getHeight() - arrowTexture.getHeight()) / 2f;
            batch.draw(arrowTexture, btnPos.x - arrowTexture.getWidth() - 20f, arrowY);
            batch.draw(arrowTexture, btnPos.x + hoveredButton.getWidth() + 20f, arrowY);
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
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (descFont != null) descFont.dispose();
        if (arrowTexture != null) arrowTexture.dispose();
        if (cornerTexture != null) cornerTexture.dispose();
        if (blankTexture != null) blankTexture.dispose();
        if (itemBorderTexture != null) itemBorderTexture.dispose();
        if (iconCompletion != null) iconCompletion.dispose();
        if (iconSpeedrun != null) iconSpeedrun.dispose();
        if (iconTrueHunter != null) iconTrueHunter.dispose();
        if (iconFalseKnight != null) iconFalseKnight.dispose();
        if (iconCustom != null) iconCustom.dispose();
        if (iconLock != null) iconLock.dispose();
        if (scrollTrackTexture != null) scrollTrackTexture.dispose();
        if (scrollKnobTexture != null) scrollKnobTexture.dispose();
        if (bgTextures != null) {
            for (Texture tex : bgTextures) {
                if (tex != null) tex.dispose();
            }
        }
    }
}
