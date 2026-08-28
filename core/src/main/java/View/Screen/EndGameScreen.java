package View.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.lwjgl3.Main;

public class EndGameScreen implements Screen {
    private final Main game;
    private final int deathCount;
    private final int enemiesKilled;
    private final float elapsedSeconds;

    private static final String NEXT_LEVEL_TMX = null;

    private Stage stage;
    private SpriteBatch batch;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 1280f;
    private final float VIRTUAL_HEIGHT = 720f;

    private BitmapFont titleFont;
    private BitmapFont statFont;
    private BitmapFont buttonFont;

    private Animation<TextureRegion> backgroundAnimation;
    private Texture[] bgTextures;
    private float stateTime = 0f;
    private Texture blankTexture;

    private Music winMusic;
    private Preferences prefs;
    private String lang;

    public EndGameScreen(Main game, int deathCount, int enemiesKilled, float elapsedSeconds) {
        this.game = game;
        this.deathCount = deathCount;
        this.enemiesKilled = enemiesKilled;
        this.elapsedSeconds = elapsedSeconds;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);

        prefs = Gdx.app.getPreferences("MyGameSettings");
        lang = prefs.getString("lang", "EN");

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = 54;
        parameter.color = Color.GOLD;
        titleFont = generator.generateFont(parameter);

        parameter.size = 26;
        parameter.color = Color.WHITE;
        statFont = generator.generateFont(parameter);

        parameter.size = 24;
        parameter.color = Color.WHITE;
        buttonFont = generator.generateFont(parameter);
        generator.dispose();

        if (Gdx.files.internal("bg_3/ezgif-frame-1.jpg").exists()) {
            bgTextures = new Texture[25];
            TextureRegion[] frames = new TextureRegion[25];
            for (int i = 0; i < 25; i++) {
                bgTextures[i] = new Texture(Gdx.files.internal("bg_3/ezgif-frame-" + (i + 1) + ".jpg"));
                frames[i] = new TextureRegion(bgTextures[i]);
            }
            backgroundAnimation = new Animation<>(0.1f, frames);
        }

        Pixmap pixmapBlank = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapBlank.setColor(new Color(0f, 0f, 0f, 0.55f));
        pixmapBlank.fill();
        blankTexture = new Texture(pixmapBlank);
        pixmapBlank.dispose();

        buildUi();
        playWinMusic();
    }

    private void buildUi() {
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        boolean es = "ES".equals(lang);

        Label title = new Label(es ? "¡VICTORIA!" : "VICTORY", new Label.LabelStyle(titleFont, Color.GOLD));
        title.setAlignment(Align.center);
        root.add(title).padBottom(40f).row();

        Table statsTable = new Table();
        Label.LabelStyle statStyle = new Label.LabelStyle(statFont, Color.LIGHT_GRAY);

        String deathsText = (es ? "Muertes: " : "Deaths: ") + deathCount;
        String killsText = (es ? "Enemigos derrotados: " : "Enemies defeated: ") + enemiesKilled;
        String timeText = (es ? "Tiempo total: " : "Total time: ") + formatTime(elapsedSeconds);

        statsTable.add(new Label(deathsText, statStyle)).padBottom(10f).row();
        statsTable.add(new Label(killsText, statStyle)).padBottom(10f).row();
        statsTable.add(new Label(timeText, statStyle)).padBottom(10f).row();

        root.add(statsTable).padBottom(50f).row();

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GOLD;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;

        TextButton restartBtn = new TextButton(es ? "REINICIAR" : "RESTART", buttonStyle);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stopWinMusic();
                game.setScreen(new GameScreen(game));
                dispose();
            }
        });

        TextButton menuBtn = new TextButton(es ? "MENÚ PRINCIPAL" : "MAIN MENU", buttonStyle);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                stopWinMusic();
                game.setScreen(new MainMenuScreen(game));
                dispose();
            }
        });

        TextButton nextMapBtn = new TextButton(es ? "SIGUIENTE MAPA" : "NEXT MAP", buttonStyle);
        boolean nextMapReady = NEXT_LEVEL_TMX != null && !NEXT_LEVEL_TMX.isBlank();
        if (!nextMapReady) {
            nextMapBtn.setDisabled(true);
            nextMapBtn.setColor(0.5f, 0.5f, 0.5f, 0.6f);
            nextMapBtn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        } else {
            nextMapBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    stopWinMusic();

                    game.setScreen(new MainMenuScreen(game));
                    dispose();
                }
            });
        }

        Table buttonsRow = new Table();
        buttonsRow.add(restartBtn).padRight(30f);
        buttonsRow.add(menuBtn).padRight(30f);
        buttonsRow.add(nextMapBtn);
        root.add(buttonsRow);
    }

    private String formatTime(float totalSeconds) {
        int total = Math.max(0, Math.round(totalSeconds));
        int minutes = total / 60;
        int seconds = total % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void playWinMusic() {
        String path = "music/victory_theme.mp3";
        if (!Gdx.files.internal(path).exists()) {
            return;
        }
        winMusic = Gdx.audio.newMusic(Gdx.files.internal(path));
        float masterVol = prefs.getFloat("master_volume", 1f);
        float musicVol = prefs.getFloat("menu_volume", 1f);
        winMusic.setVolume(masterVol * musicVol);
        winMusic.setLooping(true);
        winMusic.play();
    }

    private void stopWinMusic() {
        if (winMusic != null) {
            winMusic.stop();
            winMusic.dispose();
            winMusic = null;
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        if (backgroundAnimation != null) {
            stateTime += delta;
            TextureRegion frame = backgroundAnimation.getKeyFrame(stateTime, true);
            batch.begin();
            batch.draw(frame, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            batch.setColor(1f, 1f, 1f, 1f);
            batch.draw(blankTexture, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            batch.end();
        }

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
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
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();
        if (titleFont != null) titleFont.dispose();
        if (statFont != null) statFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (blankTexture != null) blankTexture.dispose();
        stopWinMusic();
        if (bgTextures != null) {
            for (Texture tex : bgTextures) {
                if (tex != null) tex.dispose();
            }
        }
    }
}
