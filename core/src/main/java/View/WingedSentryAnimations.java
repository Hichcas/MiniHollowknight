package View;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;

public class WingedSentryAnimations {
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> turnToFly;
    private final Animation<TextureRegion> chargeAntic;
    private final Animation<TextureRegion> chargeHorizontal;
    private final Animation<TextureRegion> deathAir;
    private final Animation<TextureRegion> deathLand;
    private final ArrayList<Texture> loadedTextures = new ArrayList<>();

    public WingedSentryAnimations() {
        idle = AnimationLoader.load("WingedSentry/Idle", 0.08f, Animation.PlayMode.LOOP, loadedTextures);
        turnToFly = AnimationLoader.load("WingedSentry/TurnToFly", 0.08f, Animation.PlayMode.NORMAL, loadedTextures);
        chargeAntic = AnimationLoader.load("WingedSentry/ChargeAntic", 0.08f, Animation.PlayMode.NORMAL, loadedTextures);
        chargeHorizontal = AnimationLoader.load("WingedSentry/ChargeHorizontal", 0.08f, Animation.PlayMode.LOOP, loadedTextures);
        deathAir = AnimationLoader.load("WingedSentry/DeathAir", 0.08f, Animation.PlayMode.NORMAL, loadedTextures);
        deathLand = AnimationLoader.load("WingedSentry/DeathLand", 0.08f, Animation.PlayMode.NORMAL, loadedTextures);
    }

    public Animation<TextureRegion> getIdle() { return idle; }
    public Animation<TextureRegion> getDeathAir() { return deathAir; }
    public Animation<TextureRegion> getDeathLand() { return deathLand; }
    public Animation<TextureRegion> getTurnToFly() { return turnToFly; }
    public Animation<TextureRegion> getChargeAntic() { return chargeAntic; }
    public Animation<TextureRegion> getChargeHorizontal() { return chargeHorizontal; }

    public void dispose() {
        for (Texture texture : loadedTextures) texture.dispose();
        loadedTextures.clear();
    }
}
