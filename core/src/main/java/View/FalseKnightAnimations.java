package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class FalseKnightAnimations {
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> turn;
    private final Animation<TextureRegion> run;
    private final Animation<TextureRegion> attackAntic;
    private final Animation<TextureRegion> attack;
    private final Animation<TextureRegion> jump;
    private final Animation<TextureRegion> jumpAttack;
    private final Animation<TextureRegion> jumpAttackHit;
    private final Animation<TextureRegion> land;
    private final Animation<TextureRegion> body;
    private final Animation<TextureRegion> stunRecover;
    private final Animation<TextureRegion> deathFall;
    private final Animation<TextureRegion> deathHit;
    private final Animation<TextureRegion> deathLand;
    private final Animation<TextureRegion> deathCorpse;
    private final ArrayList<Texture> loadedTextures = new ArrayList<>();
    private Texture placeholderTexture;

    public FalseKnightAnimations() {
        idle = loadAnimation("FalseKnight/Idle", 0.08f, true);
        turn = loadAnimation("FalseKnight/Turn", 0.08f, false);
        run = loadAnimation("FalseKnight/Run", 0.08f, true);
        attackAntic = loadAnimation("FalseKnight/AttackAntic", 0.08f, false);
        attack = loadAnimation("FalseKnight/Attack", 0.08f, false);
        jump = loadAnimation("FalseKnight/Jump", 0.08f, false);
        jumpAttack = loadFirstAvailableAnimation(0.08f, false,
            "FalseKnight/JumpAttack",
            "FalseKnight/JumpAttackAnim",
            "FalseKnight/Jump"
        );
        jumpAttackHit = loadFirstAvailableAnimation(0.08f, false,
            "FalseKnight/JumpAttackHit",
            "FalseKnight/JumpAttackImpact",
            "FalseKnight/Land"
        );
        land = loadAnimation("FalseKnight/Land", 0.08f, false);
        body = loadAnimation("FalseKnight/Body", 0.08f, true);
        stunRecover = loadAnimation("FalseKnight/StunRecover", 0.08f, false);
        deathFall = loadAnimation("FalseKnight/DeathFall", 0.08f, false);
        deathHit = loadAnimation("FalseKnight/DeathHit", 0.08f, false);
        deathLand = loadAnimation("FalseKnight/DeathLand", 0.08f, false);
        deathCorpse = loadAnimation("FalseKnight/DeathCorpse", 0.08f, false);
    }

    private Animation<TextureRegion> loadFirstAvailableAnimation(float frameDuration, boolean loop, String... folders) {
        for (String folder : folders) {
            Animation<TextureRegion> candidate = loadAnimation(folder, frameDuration, loop);
            if (candidate.getKeyFrames().length > 0) {
                return candidate;
            }
        }
        return loadAnimation(folders.length > 0 ? folders[0] : "", frameDuration, loop);
    }

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration, boolean loop) {
        return AnimationLoader.load(
            folderName,
            frameDuration,
            loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL,
            loadedTextures
        );
    }

    public Animation<TextureRegion> getIdle() {
        return idle;
    }

    public Animation<TextureRegion> getTurn() {
        return turn;
    }

    public Animation<TextureRegion> getRun() {
        return run;
    }

    public Animation<TextureRegion> getAttackAntic() {
        return attackAntic;
    }

    public Animation<TextureRegion> getAttack() {
        return attack;
    }

    public Animation<TextureRegion> getJump() {
        return jump;
    }

    public Animation<TextureRegion> getJumpAttack() {
        return jumpAttack;
    }

    public Animation<TextureRegion> getJumpAttackHit() {
        return jumpAttackHit;
    }

    public Animation<TextureRegion> getLand() {
        return land;
    }

    public Animation<TextureRegion> getBody() {
        return body;
    }

    public Animation<TextureRegion> getStunRecover() {
        return stunRecover;
    }

    public Animation<TextureRegion> getDeathFall() {
        return deathFall;
    }

    public Animation<TextureRegion> getDeathHit() {
        return deathHit;
    }

    public Animation<TextureRegion> getDeathLand() {
        return deathLand;
    }

    public Animation<TextureRegion> getDeathCorpse() {
        return deathCorpse;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            if (texture != null) {
                texture.dispose();
            }
        }
        loadedTextures.clear();
    }
}
