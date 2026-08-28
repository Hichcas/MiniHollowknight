package View.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.lwjgl3.Main;

public class GuideScreen implements Screen {
    private final Main game;
    private final Screen mainMenuScreen;
    private final Stage stage;
    private final Preferences prefs;
    private String lang;

    private SpriteBatch batch;
    private Animation<TextureRegion> backgroundAnimation;
    private float stateTime = 0f;
    private Texture[] bgTextures;

    private BitmapFont titleFont;
    private BitmapFont mainFont;
    private BitmapFont smallFont;

    private Label.LabelStyle titleStyle;
    private Label.LabelStyle subtitleStyle;
    private Label.LabelStyle defaultStyle;
    private Label.LabelStyle highlightStyle;
    private Label.LabelStyle smallStyle;
    private Label.LabelStyle codeStyle;
    private TextButton.TextButtonStyle buttonStyle;

    private Table mainTable;
    private Table controlsTable;
    private Table abilitiesTable;
    private Table cheatsTable;

    private Texture keyBoxTexture;
    private Drawable keyBoxDrawable;
    private static final float KEY_BOX_HOVER_SCALE = 1.18f;
    private static final float KEY_BOX_ANIM_DURATION = 0.12f;

    public GuideScreen(final Main game, final Screen mainMenuScreen) {
        this.game = game;
        this.mainMenuScreen = mainMenuScreen;
        this.stage = new Stage(new ScreenViewport());
        this.prefs = Gdx.app.getPreferences("MyGameSettings");
        this.lang = prefs.getString("lang", "EN");

        initStyles();
    }

    private void initStyles() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = 36;
        parameter.color = Color.WHITE;
        titleFont = generator.generateFont(parameter);

        parameter.size = 24;
        mainFont = generator.generateFont(parameter);

        parameter.size = 18;
        smallFont = generator.generateFont(parameter);

        generator.dispose();

        titleStyle = new Label.LabelStyle(titleFont, Color.GOLD);
        subtitleStyle = new Label.LabelStyle(mainFont, Color.valueOf("e0a96d"));
        defaultStyle = new Label.LabelStyle(mainFont, Color.WHITE);
        highlightStyle = new Label.LabelStyle(mainFont, Color.GOLD);
        smallStyle = new Label.LabelStyle(smallFont, Color.WHITE);
        codeStyle = new Label.LabelStyle(smallFont, Color.CYAN);

        buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = mainFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GOLD;
        buttonStyle.downFontColor = Color.WHITE;

        createKeyBoxDrawable();
    }

    private void createKeyBoxDrawable() {
        int w = 64, h = 40;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.16f, 0.14f, 0.1f, 0.92f);
        pixmap.fill();
        pixmap.setColor(Color.valueOf("e0a96d"));
        pixmap.drawRectangle(0, 0, w, h);
        pixmap.drawRectangle(1, 1, w - 2, h - 2);
        keyBoxTexture = new Texture(pixmap);
        pixmap.dispose();
        keyBoxDrawable = new TextureRegionDrawable(new TextureRegion(keyBoxTexture));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        this.lang = prefs.getString("lang", "EN");

        batch = new SpriteBatch();
        bgTextures = new Texture[25];
        TextureRegion[] bgFrames = new TextureRegion[25];
        for (int i = 0; i < 25; i++) {
            bgTextures[i] = new Texture(Gdx.files.internal("bg_2" + "/ezgif-frame-" + (i + 1) + ".jpg"));
            bgFrames[i] = new TextureRegion(bgTextures[i]);
        }
        backgroundAnimation = new Animation<>(0.1f, bgFrames);

        stage.clear();
        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20);
        stage.addActor(mainTable);

        String titleText = lang.equals("ES") ? "MENÚ DE GUÍA" : "GUIDE MENU";
        Label titleLabel = new Label(titleText, titleStyle);
        titleLabel.setAlignment(Align.center);
        mainTable.add(titleLabel).padBottom(20).colspan(3).row();

        createControlsSection();
        createAbilitiesSection();
        createCheatsSection();

        Table contentContainer = new Table();
        contentContainer.add(controlsTable).top().pad(10).width(350);
        contentContainer.add(abilitiesTable).top().pad(10).width(450);
        contentContainer.add(cheatsTable).top().pad(10).width(350);

        ScrollPane scrollPane = new ScrollPane(contentContainer, new ScrollPane.ScrollPaneStyle());
        scrollPane.setFadeScrollBars(true);

        mainTable.add(scrollPane).expand().fill().padBottom(20).row();

        String backText = lang.equals("ES") ? "VOLVER AL MENÚ" : "Back to Main Menu";
        TextButton backButton = new TextButton(backText, buttonStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(mainMenuScreen);
            }
        });
        mainTable.add(backButton).center().width(250).height(50);
    }

    private void createControlsSection() {
        controlsTable = new Table();
        controlsTable.top().left();

        String header = lang.equals("ES") ? "🎮 Controles del Juego" : "🎮 Game Controls";
        Label sectionHeader = new Label(header, subtitleStyle);
        controlsTable.add(sectionHeader).padBottom(15).colspan(2).left().row();

        String keyLeft = Input.Keys.toString(prefs.getInteger("key_LEFT", Input.Keys.A));
        String keyRight = Input.Keys.toString(prefs.getInteger("key_RIGHT", Input.Keys.D));
        String keyJump = Input.Keys.toString(prefs.getInteger("key_JUMP", Input.Keys.SPACE));
        String keyDash = Input.Keys.toString(prefs.getInteger("key_DASH", Input.Keys.R));
        String keyAttack = Input.Keys.toString(prefs.getInteger("key_ATTACK", Input.Keys.X));
        String keyFocus = Input.Keys.toString(prefs.getInteger("key_FOCUS", Input.Keys.F));

        if (lang.equals("ES")) {
            addControlRow("Mover a la Izquierda:", keyLeft);
            addControlRow("Mover a la Derecha:", keyRight);
            addControlRow("Saltar:", keyJump);
            addControlRow("Esquivar (Dash):", keyDash);
            addControlRow("Ataque Aguijón (Nail):", keyAttack);
            addControlRow("Concentración (Focus):", keyFocus);
        } else {
            addControlRow("Move Left:", keyLeft);
            addControlRow("Move Right:", keyRight);
            addControlRow("Jump:", keyJump);
            addControlRow("Dash:", keyDash);
            addControlRow("Nail Attack:", keyAttack);
            addControlRow("Focus / Heal:", keyFocus);
        }
    }

    private void addControlRow(String actionName, String keyName) {
        Label actionLabel = new Label(actionName, defaultStyle);
        Table keyBox = createKeyBox(keyName);
        controlsTable.add(actionLabel).pad(5).left();
        controlsTable.add(keyBox).pad(5).right().row();
    }

    private Table createKeyBox(String keyName) {
        Label keyLabel = new Label(keyName, highlightStyle);
        keyLabel.setAlignment(Align.center);

        final Table box = new Table();
        box.setBackground(keyBoxDrawable);
        box.add(keyLabel).pad(6, 12, 6, 12);
        box.pack();

        box.setTransform(true);
        box.setOrigin(Align.center);
        box.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);

        box.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer != -1) return;
                box.clearActions();
                box.addAction(Actions.scaleTo(KEY_BOX_HOVER_SCALE, KEY_BOX_HOVER_SCALE,
                    KEY_BOX_ANIM_DURATION, Interpolation.pow2Out));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer != -1) return;
                box.clearActions();
                box.addAction(Actions.scaleTo(1f, 1f,
                    KEY_BOX_ANIM_DURATION, Interpolation.pow2Out));
            }
        });

        return box;
    }

    private void createAbilitiesSection() {
        abilitiesTable = new Table();
        abilitiesTable.top().left();

        String header = lang.equals("ES") ? "⚔️ Habilidades del Caballero" : "⚔️ Knight Abilities & Systems";
        Label sectionHeader = new Label(header, subtitleStyle);
        abilitiesTable.add(sectionHeader).padBottom(15).left().row();

        String abilitiesText;
        if (lang.equals("ES")) {
            abilitiesText =
                "• Sistema de Máscaras (HP & Masks):\n" +
                    "Salud discreta representada por 5 Máscaras. Recibir daño de enemigos o espinas elimina 1 Máscara, " +
                    "activando 1 segundo de invencibilidad visual. Quedarse sin máscaras causa reaparición en el inicio.\n\n" +
                    "• Vasija de Alma (Soul Vessel):\n" +
                    "Un contenedor que almacena hasta 99 unidades de alma. Cada golpe exitoso con el Aguijón (Nail) " +
                    "otorga 11 unidades de alma. El alma se mantiene entre transiciones de habitaciones.\n\n" +
                    "• Concentración (Focus):\n" +
                    "Mantén presionado el botón de enfoque en el suelo para consumir alma y curar las máscaras در las que has sufrido daño.\n\n" +
                    "• Movimiento y Hechizos:\n" +
                    "- Doble Salto (Double Jump): Realiza un segundo salto en el aire con una animación diferente.\n" +
                    "- Garra de Mantis (Mantis Claw): Deslízate por paredes verticales suavemente.\n" +
                    "- Salto Pogo (Pogo Jumping): Ataca hacia abajo sobre espinas o enemigos para rebotar y reiniciar " +
                    "los tiempos de recarga de Dash y Doble Salto.";
        } else {
            abilitiesText =
                "• Health System (HP & Masks):\n" +
                    "Discrete health represented by 5 Masks. Taking damage from mobs or spikes removes 1 Mask, " +
                    "triggering 1 second of invincibility. Zero masks causes respawning at the start.\n\n" +
                    "• Soul Vessel System:\n" +
                    "A container holding up to 99 soul units. Each successful Nail strike on enemies grants 11 soul. " +
                    "Soul status persists across room transitions.\n\n" +
                    "• Focus Mechanic:\n" +
                    "Hold the focus button while stationary on the ground to consume soul and heal damaged masks.\n\n" +
                    "• Movement & Spells:\n" +
                    "- Double Jump: Perform a secondary jump mid-air with distinct animation.\n" +
                    "- Mantis Claw: Slide down vertical walls smoothly.\n" +
                    "- Pogo Jumping: Slash downwards on spikes or mobs to bounce up and reset Dash/Double Jump cooldowns.";
        }

        Label detailsLabel = new Label(abilitiesText, smallStyle);
        detailsLabel.setWrap(true);
        abilitiesTable.add(detailsLabel).width(430).fillX().left();
    }

    private void createCheatsSection() {
        cheatsTable = new Table();
        cheatsTable.top().left();

        String header = lang.equals("ES") ? "📜 Códigos de Trampa" : "📜 Cheat Codes";
        Label sectionHeader = new Label(header, subtitleStyle);
        cheatsTable.add(sectionHeader).padBottom(15).colspan(2).left().row();

        if (lang.equals("ES")) {
            addCheatRow("Ctrl + B", "Teletransporte instantáneo a la arena del jefe (boss teleport)");
            addCheatRow("Ctrl + C", "Modo Creativo: vuela a cualquier parte del mapa sin gravedad ni colisiones, como en Minecraft (creative)");
            addCheatRow("Ctrl + G", "Salud infinita e invincibilidad completa (godmode)");
            addCheatRow("Ctrl + H", "Restaura instantáneamente todas las máscaras de salud (heal)");
            addCheatRow("Ctrl + S", "Llena instantáneamente la vasija de alma a 99 (infsoul)");
            addCheatRow("Ctrl + U", "Desbloquea todas las habilidades y amuletos (unlockall)");
            addCheatRow("Ctrl + K", "Elimina instantáneamente a todos los enemigos en la sala actual (killall)");
            addCheatRow("Ctrl + M", "Activa/desactiva la Curación de Emergencia: la próxima vez que se rompa tu última máscara, obtienes 1 máscara extra en vez de morir (un solo uso, se desactiva solo después de usarse)");
        } else {
            addCheatRow("Ctrl + B", "Instantly teleports to the boss arena (boss teleport)");
            addCheatRow("Ctrl + C", "Creative Mode: fly anywhere on the map with no gravity or collisions, like in Minecraft (creative)");
            addCheatRow("Ctrl + G", "Infinite health and complete invincibility (godmode)");
            addCheatRow("Ctrl + H", "Instantly restores all health masks (heal)");
            addCheatRow("Ctrl + S", "Instantly maxes out the soul vessel to 99 (infsoul)");
            addCheatRow("Ctrl + U", "Unlocks all character abilities and charms (unlockall)");
            addCheatRow("Ctrl + K", "Instantly slays all active mobs in the current room (killall)");
            addCheatRow("Ctrl + M", "Toggles Emergency Heal: next time your last mask breaks, you get 1 extra mask instead of dying (single-use, auto-disarms after triggering)");
        }
    }

    private void addCheatRow(String code, String effect) {
        Table keyBox = createKeyBox(code);
        Label effectLabel = new Label(effect, smallStyle);
        effectLabel.setWrap(true);

        cheatsTable.add(keyBox).pad(5).left().top();
        cheatsTable.add(effectLabel).pad(5).left().width(220).row();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stateTime += delta;
        TextureRegion currentFrame = backgroundAnimation.getKeyFrame(stateTime, true);

        batch.setProjectionMatrix(stage.getViewport().getCamera().combined);
        batch.begin();
        batch.draw(currentFrame, 0, 0, stage.getViewport().getWorldWidth(), stage.getViewport().getWorldHeight());
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
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
        stage.dispose();
        if (titleFont != null) titleFont.dispose();
        if (mainFont != null) mainFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (batch != null) batch.dispose();
        if (keyBoxTexture != null) keyBoxTexture.dispose();
        if (bgTextures != null) {
            for (Texture tex : bgTextures) {
                if (tex != null) tex.dispose();
            }
        }
    }
}
