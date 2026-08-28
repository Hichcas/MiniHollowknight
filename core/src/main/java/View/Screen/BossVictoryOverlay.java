package View.Screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class BossVictoryOverlay {

    public interface Callback {
        void onRestart();

        void onMainMenu();
    }

    private Stage stage;
    private BitmapFont titleFont;
    private BitmapFont statFont;
    private BitmapFont buttonFont;
    private BitmapFont countdownFont;
    private Texture dimTexture;
    private Label countdownLabel;
    private String countdownTemplate = "Game will continue in %d seconds";

    private boolean active = false;
    private float timer = 0f;
    private float duration = 8f;
    private Callback callback;
    private boolean finished = false;

    public void init() {
        stage = new Stage(new ScreenViewport());

        Pixmap dim = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        dim.setColor(0f, 0f, 0f, 0.65f);
        dim.fill();
        dimTexture = new Texture(dim);
        dim.dispose();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = 40;
        parameter.color = Color.GOLD;
        titleFont = generator.generateFont(parameter);

        parameter.size = 22;
        parameter.color = Color.WHITE;
        statFont = generator.generateFont(parameter);

        parameter.size = 20;
        parameter.color = Color.WHITE;
        buttonFont = generator.generateFont(parameter);

        parameter.size = 18;
        parameter.color = Color.LIGHT_GRAY;
        countdownFont = generator.generateFont(parameter);
        generator.dispose();
    }

    public void show(String title, String[] statLines, float durationSeconds,
                     String restartLabel, String menuLabel, String countdownTemplate, Callback callback) {
        if (stage == null) {
            init();
        }
        this.callback = callback;
        this.duration = durationSeconds;
        this.timer = 0f;
        this.active = true;
        this.finished = false;
        this.countdownTemplate = countdownTemplate != null ? countdownTemplate : "Game will continue in %d seconds";

        stage.clear();

        Table dimTable = new Table();
        dimTable.setFillParent(true);
        dimTable.setBackground(new TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(dimTexture)));
        stage.addActor(dimTable);

        Table root = new Table();
        root.setFillParent(true);
        root.center();
        stage.addActor(root);

        Label titleLabel = new Label(title, new Label.LabelStyle(titleFont, Color.GOLD));
        titleLabel.setAlignment(Align.center);
        root.add(titleLabel).padBottom(25f).row();

        for (String line : statLines) {
            Label l = new Label(line, new Label.LabelStyle(statFont, Color.LIGHT_GRAY));
            l.setAlignment(Align.center);
            root.add(l).padBottom(8f).row();
        }

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.overFontColor = Color.GOLD;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;

        Table buttonsRow = new Table();

        TextButton restartBtn = new TextButton(restartLabel, buttonStyle);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                finish();
                if (callback != null) callback.onRestart();
            }
        });

        TextButton menuBtn = new TextButton(menuLabel, buttonStyle);
        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                finish();
                if (callback != null) callback.onMainMenu();
            }
        });

        buttonsRow.add(restartBtn).padRight(25f);
        buttonsRow.add(menuBtn);
        root.add(buttonsRow).padTop(30f).row();

        countdownLabel = new Label(String.format(this.countdownTemplate, Math.round(duration)),
            new Label.LabelStyle(countdownFont, Color.LIGHT_GRAY));
        countdownLabel.setAlignment(Align.center);
        root.add(countdownLabel).padTop(18f);
    }

    public boolean isActive() {
        return active;
    }

    public boolean consumeAutoFinished() {
        if (finished && active) {
            active = false;
            return true;
        }
        return false;
    }

    public void update(float delta) {
        if (!active) {
            return;
        }
        timer += delta;
        if (countdownLabel != null) {
            int secondsLeft = Math.max(0, (int) Math.ceil(duration - timer));
            countdownLabel.setText(String.format(countdownTemplate, secondsLeft));
        }
        if (timer >= duration) {
            finished = true;
        }
    }

    private void finish() {
        active = false;
    }

    public Stage getStage() {
        return stage;
    }

    public void render() {
        if (stage == null) {
            return;
        }
        stage.act();
        stage.draw();
    }

    public void resize(int width, int height) {
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    public void dispose() {
        if (stage != null) stage.dispose();
        if (titleFont != null) titleFont.dispose();
        if (statFont != null) statFont.dispose();
        if (buttonFont != null) buttonFont.dispose();
        if (countdownFont != null) countdownFont.dispose();
        if (dimTexture != null) dimTexture.dispose();
    }
}
