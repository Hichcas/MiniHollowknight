package View.Screen;

import Controller.CharmManager;
import Model.CharmDefinition;
import Model.Enums.CharmState;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.lwjgl3.Main;

import java.util.EnumMap;
import java.util.Map;

public class CharmScreen implements Screen {
    private static final float VIRTUAL_WIDTH = 1280f;
    private static final float VIRTUAL_HEIGHT = 720f;
    private static final int GRID_COLUMNS = 4;

    private final Main game;
    private final Screen returnScreen;
    private final CharmManager charmManager;

    private Stage stage;
    private Viewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont smallFont;
    private Preferences prefs;

    private Table root;
    private Table topBar;
    private Table gridTable;
    private Table detailsPanel;

    private final Map<CharmState, ImageButton> charmButtons = new EnumMap<>(CharmState.class);
    private final Map<CharmState, Image> charmGlow = new EnumMap<>(CharmState.class);
    private final Map<CharmState, Image> disabledOverlays = new EnumMap<>(CharmState.class);
    private final Map<CharmState, Image> charmIcons = new EnumMap<>(CharmState.class);
    private final Map<CharmState, Texture> charmIconTextures = new EnumMap<>(CharmState.class);
    private final Image[] notchSlots = new Image[3];
    private Image detailIcon;

    private Label titleLabel;
    private Label costLabel;
    private Label descLabel;
    private Label notchInfoLabel;
    private TextButton backButton;

    private Texture panelTexture;
    private Texture panelBorderTexture;
    private Texture slotTexture;
    private Texture slotGlowTexture;
    private Texture selectedFrameTexture;
    private Texture disabledOverlayTexture;

    private CharmState hoveredState = null;

    public CharmScreen(Main game, Screen returnScreen, CharmManager charmManager) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.charmManager = charmManager;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);
        prefs = Gdx.app.getPreferences("MyGameSettings");

        loadFonts();
        loadTextures();
        buildUI();
        refreshAll();
    }

    private void loadFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("OptimusPrincepsSemiBold.ttf"));
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();

        parameter.size = 48;
        parameter.color = Color.WHITE;
        titleFont = generator.generateFont(parameter);

        parameter.size = 24;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);

        parameter.size = 18;
        parameter.color = Color.LIGHT_GRAY;
        smallFont = generator.generateFont(parameter);

        generator.dispose();
    }

    private Texture loadOrCreate(String path, Color fallback) {
        if (Gdx.files.internal(path).exists()) {
            return new Texture(Gdx.files.internal(path));
        }
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(fallback);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture makeSolid(Color color) {
        Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private void loadTextures() {
        panelTexture = loadOrCreate("ui/charm_panel.png", new Color(0.05f, 0.05f, 0.05f, 0.92f));
        panelBorderTexture = loadOrCreate("ui/charm_border.png", new Color(1f, 1f, 1f, 0.18f));
        slotTexture = loadOrCreate("ui/charm_slot.png", new Color(0.18f, 0.18f, 0.18f, 0.95f));
        slotGlowTexture = loadOrCreate("ui/charm_slot_glow.png", new Color(0.75f, 0.85f, 1f, 0.35f));
        selectedFrameTexture = loadOrCreate("ui/charm_selected.png", new Color(1f, 1f, 1f, 0.55f));
        disabledOverlayTexture = loadOrCreate("ui/charm_disabled.png", new Color(0f, 0f, 0f, 0.55f));

        for (CharmState state : CharmState.values()) {
            charmIconTextures.put(state, loadCharmIconTexture(state));
        }
    }

    private Drawable drawable(Texture tex) {
        return new TextureRegionDrawable(new TextureRegion(tex));
    }

    private void buildUI() {
        root = new Table();
        root.setFillParent(true);
        root.top().pad(28f, 36f, 24f, 36f);
        stage.addActor(root);

        Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, Color.WHITE);
        Label.LabelStyle mainStyle = new Label.LabelStyle(font, Color.WHITE);
        Label.LabelStyle smallStyle = new Label.LabelStyle(smallFont, Color.LIGHT_GRAY);

        topBar = new Table();
        topBar.left();
        Label notchTitle = new Label("Notches", mainStyle);
        topBar.add(notchTitle).left().padRight(18f);
        for (int i = 0; i < notchSlots.length; i++) {
            Image img = new Image(slotTexture);
            img.setSize(28f, 28f);
            notchSlots[i] = img;
            topBar.add(img).size(28f).padRight(10f);
        }
        root.add(topBar).expandX().left().row();

        root.add(new Image(panelBorderTexture)).height(2f).growX().padTop(16f).padBottom(14f).row();

        Table body = new Table();
        body.setFillParent(false);
        body.defaults().top();

        gridTable = new Table();
        gridTable.top().left();
        for (CharmState state : CharmState.values()) {
            gridTable.add(createCharmCell(state)).size(120f).pad(10f);
            if ((state.ordinal() + 1) % GRID_COLUMNS == 0) {
                gridTable.row();
            }
        }
        detailsPanel = new Table();
        detailsPanel.top().left().padLeft(24f);
        detailsPanel.setBackground(new TextureRegionDrawable(new TextureRegion(panelTexture)));
        detailsPanel.pad(22f);
        titleLabel = new Label("", titleStyle);
        titleLabel.setWrap(true);
        titleLabel.setAlignment(Align.left);
        costLabel = new Label("", mainStyle);
        notchInfoLabel = new Label("", smallStyle);
        descLabel = new Label("", smallStyle);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.left);
        detailsPanel.add(titleLabel).left().width(420f).row();
        detailsPanel.add(costLabel).left().padTop(12f).row();
        detailIcon = new Image(charmIconTextures.get(CharmState.SOUL_CATCHER));
        detailsPanel.add(detailIcon).size(108f).padTop(20f).padBottom(16f).row();
        detailsPanel.add(notchInfoLabel).left().width(420f).padBottom(10f).row();
        detailsPanel.add(descLabel).left().width(420f).padTop(10f).row();
        body.add(gridTable).left().top().padRight(30f);
        body.add(detailsPanel).width(470f).fillY().expandX().right();
        root.add(body).expand().fill().padTop(8f).row();

        Table footer = new Table();
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.WHITE;
        btnStyle.overFontColor = Color.GOLD;
        btnStyle.downFontColor = Color.LIGHT_GRAY;
        backButton = new TextButton("Back", btnStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                closeMenu();
            }
        });
        footer.add(backButton).right().padTop(16f);
        root.add(footer).expandX().right().padTop(8f).row();
    }

    private Actor createCharmCell(final CharmState state) {
        final Table wrapper = new Table();
        wrapper.setTransform(true);

        final Image bg = new Image(slotTexture);
        bg.setSize(108f, 108f);

        final Image glow = new Image(slotGlowTexture);
        glow.setSize(120f, 120f);
        glow.setVisible(false);
        charmGlow.put(state, glow);

        final Image icon = new Image(charmIconTextures.get(state));
        icon.setSize(70f, 70f);
        charmIcons.put(state, icon);

        final Image selected = new Image(selectedFrameTexture);
        selected.setSize(120f, 120f);
        selected.setVisible(false);

        final Image disabled = new Image(disabledOverlayTexture);
        disabled.setSize(108f, 108f);
        disabled.setVisible(false);
        disabledOverlays.put(state, disabled);

        wrapper.addActor(glow);
        wrapper.addActor(bg);
        wrapper.addActor(icon);
        wrapper.addActor(selected);
        wrapper.addActor(disabled);

        icon.setPosition(19f, 19f);
        glow.setPosition(-6f, -6f);
        selected.setPosition(-6f, -6f);
        disabled.setPosition(0f, 0f);
        bg.setPosition(0f, 0f);

        ImageButton button = new ImageButton(new TextureRegionDrawable(new TextureRegion(slotTexture)));
        button.setSize(108f, 108f);
        button.setColor(1f, 1f, 1f, 0f);
        charmButtons.put(state, button);
        wrapper.addActor(button);

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                hoveredState = state;
                refreshDetails();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (hoveredState == state) {
                    hoveredState = null;
                    refreshDetails();
                }
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean ok = charmManager.toggle(state);
                if (!ok) {
                    return;
                }
                refreshAll();
            }
        });

        wrapper.pack();
        return wrapper;
    }

    private Texture loadCharmIconTexture(CharmState state) {
        String base = "charm/" + state.name().toLowerCase();
        Texture tex = null;
        if (Gdx.files.internal(base + ".png").exists()) {
            tex = new Texture(Gdx.files.internal(base + ".png"));
        } else if (Gdx.files.internal(base + ".jpg").exists()) {
            tex = new Texture(Gdx.files.internal(base + ".jpg"));
        }
        if (tex != null) return tex;

        switch (state) {
            case SOUL_CATCHER:
                return makeSolid(new Color(0.82f, 0.82f, 0.85f, 1f));
            case DASHMASTER:
                return makeSolid(new Color(0.35f, 0.65f, 1f, 1f));
            case UNBREAKABLE_STRENGTH:
                return makeSolid(new Color(1f, 0.66f, 0.18f, 1f));
            case QUICK_SLASH:
                return makeSolid(new Color(0.92f, 0.92f, 0.92f, 1f));
            case QUICK_FOCUS:
                return makeSolid(new Color(0.78f, 0.95f, 0.85f, 1f));
            case HEAVY_BLOW:
                return makeSolid(new Color(0.9f, 0.42f, 0.42f, 1f));
            case SHARP_SHADOW:
                return makeSolid(new Color(0.42f, 0.42f, 0.62f, 1f));
            case VOID_HEART:
                return makeSolid(new Color(0.15f, 0.15f, 0.15f, 1f));
            default:
                return makeSolid(Color.WHITE);
        }
    }

    private void refreshAll() {
        refreshNotches();
        refreshGridStates();
        refreshDetails();
    }

    private void refreshNotches() {
        java.util.List<CharmState> filled = new java.util.ArrayList<>();
        for (CharmState state : CharmState.values()) {
            if (charmManager.isEquipped(state)) {
                CharmDefinition def = charmManager.getDefinition(state);
                for (int n = 0; n < def.notches; n++) {
                    filled.add(state);
                }
            }
        }

        for (int i = 0; i < notchSlots.length; i++) {
            if (i < filled.size()) {
                Texture iconTex = charmIconTextures.get(filled.get(i));
                notchSlots[i].setDrawable(new TextureRegionDrawable(new TextureRegion(iconTex)));
                notchSlots[i].setColor(1f, 1f, 1f, 1f);
            } else {
                notchSlots[i].setDrawable(new TextureRegionDrawable(new TextureRegion(slotTexture)));
                notchSlots[i].setColor(0.35f, 0.35f, 0.35f, 1f);
            }
        }
    }

    private void refreshGridStates() {
        int usedNotches = charmManager.usedNotches();

        for (CharmState state : CharmState.values()) {
            CharmDefinition def = charmManager.getDefinition(state);
            boolean equipped = charmManager.isEquipped(state);
            boolean unlockedCharm = charmManager.isUnlocked(state);
            boolean canEquip = equipped || (unlockedCharm && usedNotches + def.notches <= 3);

            Image glow = charmGlow.get(state);
            Image disabled = disabledOverlays.get(state);
            Image icon = charmIcons.get(state);

            if (glow != null) {
                glow.setVisible(equipped);
            }
            if (icon != null) {
                if (!unlockedCharm) {
                    icon.setColor(0.12f, 0.12f, 0.12f, 1f);
                } else if (equipped) {
                    icon.setColor(1f, 1f, 1f, 1f);
                } else if (canEquip) {
                    icon.setColor(0.8f, 0.8f, 0.8f, 1f);
                } else {
                    icon.setColor(0.4f, 0.4f, 0.4f, 1f);
                }
            }
            if (disabled != null) {
                disabled.setVisible(!unlockedCharm || (!equipped && !canEquip));
            }
        }
    }

    private void refreshDetails() {
        CharmState state = hoveredState;
        if (state == null) {
            for (CharmState s : CharmState.values()) {
                if (charmManager.isEquipped(s)) {
                    state = s;
                    break;
                }
            }
            if (state == null) {
                state = CharmState.SOUL_CATCHER;
            }
        }

        CharmDefinition def = charmManager.getDefinition(state);
        boolean equipped = charmManager.isEquipped(state);

        titleLabel.setText(def.name + (equipped ? "  (Equipped)" : ""));
        costLabel.setText("Notches: " + def.notches);
        notchInfoLabel.setText("Used notches: " + charmManager.usedNotches() + " / 3");
        descLabel.setText(def.description);

        Texture iconTex = charmIconTextures.get(state);
        if (detailIcon != null && iconTex != null) {
            detailIcon.setDrawable(new TextureRegionDrawable(new TextureRegion(iconTex)));
        }

        for (Map.Entry<CharmState, Image> e : charmGlow.entrySet()) {
            e.getValue().setVisible(e.getKey() == state || charmManager.isEquipped(e.getKey()));
        }
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.I)
            || Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE)) {
            closeMenu();
            return;
        }
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(delta);
        stage.draw();
    }

    private void closeMenu() {
        if (returnScreen instanceof GameScreen) {
            ((GameScreen) returnScreen).setCurrentState(Model.Enums.GameState.RUNNING);
        }
        game.setScreen(returnScreen);
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
        dispose();
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (batch != null) batch.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (smallFont != null) smallFont.dispose();
        if (panelTexture != null) panelTexture.dispose();
        if (panelBorderTexture != null) panelBorderTexture.dispose();
        if (slotTexture != null) slotTexture.dispose();
        if (slotGlowTexture != null) slotGlowTexture.dispose();
        if (selectedFrameTexture != null) selectedFrameTexture.dispose();
        if (disabledOverlayTexture != null) disabledOverlayTexture.dispose();
        for (Texture tex : charmIconTextures.values()) {
            if (tex != null) tex.dispose();
        }
    }
}
