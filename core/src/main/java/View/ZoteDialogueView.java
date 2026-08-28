package View;

import Model.DialogueBox;
import Model.Zote;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;

public class ZoteDialogueView {
    private final Zote zote;
    private final DialogueBox dialogueBox;

    private final SpriteBatch hudBatch = new SpriteBatch();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final GlyphLayout layout = new GlyphLayout();

    private BitmapFont dialogueFont;
    private BitmapFont promptFont;

    private static final float BOX_HEIGHT = 130f;
    private static final float BOX_MARGIN = 60f;

    public ZoteDialogueView(Zote zote, DialogueBox dialogueBox) {
        this.zote = zote;
        this.dialogueBox = dialogueBox;
        createFonts();
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

        param.size = 26;
        param.color = Color.WHITE;
        dialogueFont = generator.generateFont(param);

        FreeTypeFontGenerator.FreeTypeFontParameter promptParam = new FreeTypeFontGenerator.FreeTypeFontParameter();
        promptParam.size = 22;
        promptParam.color = Color.YELLOW;
        promptFont = generator.generateFont(promptParam);

        generator.dispose();
    }

    public void resize(int width, int height) {
        hudCamera.setToOrtho(false, width, height);
        hudCamera.update();
    }

    public void render(OrthographicCamera worldCamera) {
        boolean showPrompt = zote.isPlayerNearby() && !dialogueBox.isVisible();
        boolean showBox = dialogueBox.isVisible();

        if (!showPrompt && !showBox) {
            return;
        }

        hudBatch.setProjectionMatrix(hudCamera.combined);

        if (showBox) {
            Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(hudCamera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0f, 0f, 0f, 0.75f);
            float boxWidth = hudCamera.viewportWidth - BOX_MARGIN * 2f;
            shapeRenderer.rect(BOX_MARGIN, BOX_MARGIN, boxWidth, BOX_HEIGHT);
            shapeRenderer.end();
        }

        hudBatch.begin();

        if (showPrompt) {
            Vector3 screenPos = worldCamera.project(new Vector3(
                zote.getPosition().x,
                zote.getPosition().y + zote.getHeight() / 2f + 0.6f,
                0f
            ));
            String text = "Press W to know things that maybe you cant know them easily";
            layout.setText(promptFont, text);
            promptFont.draw(hudBatch, text, screenPos.x - layout.width / 2f, screenPos.y);
        }

        if (showBox) {
            float boxWidth = hudCamera.viewportWidth - BOX_MARGIN * 2f;
            dialogueFont.draw(
                hudBatch,
                dialogueBox.getVisibleText(),
                BOX_MARGIN + 30f,
                BOX_MARGIN + BOX_HEIGHT - 30f,
                boxWidth - 60f,
                com.badlogic.gdx.utils.Align.left,
                true
            );
        }

        hudBatch.end();
    }

    public void dispose() {
        dialogueFont.dispose();
        promptFont.dispose();
        hudBatch.dispose();
        shapeRenderer.dispose();
    }
}
