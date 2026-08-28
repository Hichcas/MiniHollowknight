package View;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class HuskHornheadAnimations {
    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> turn;
    private final Animation<TextureRegion> recover;
    private final Animation<TextureRegion> attackAnticipate;
    private final Animation<TextureRegion> attackLunge;
    private final Animation<TextureRegion> death;
    private final ArrayList<Texture> loadedTextures = new ArrayList<>();

    private Animation<TextureRegion> loadAnimation(String folderName, float frameDuration, boolean loop) {
        return AnimationLoader.load(
            folderName,
            frameDuration,
            loop ? Animation.PlayMode.LOOP : Animation.PlayMode.NORMAL,
            loadedTextures
        );
    }

    public HuskHornheadAnimations() {
        walk = loadAnimation("Huskhornhead/Walk", 0.08f, true);
        turn = loadAnimation("Huskhornhead/Turn", 0.08f, false);
        recover = loadAnimation("Huskhornhead/Idle", 0.08f, false);
        attackAnticipate = loadAnimation("Huskhornhead/AttackAnticipate", 0.08f, true);
        attackLunge = loadAnimation("Huskhornhead/AttackLunge", 0.08f, false);
        death = loadAnimation("Huskhornhead/Death", 0.08f, false);
    }

    public Animation<TextureRegion> getWalk() {
        return walk;
    }

    public Animation<TextureRegion> getTurn() {
        return turn;
    }

    public Animation<TextureRegion> getRecover() {
        return recover;
    }

    public Animation<TextureRegion> getAttackAnticipate() {
        return attackAnticipate;
    }

    public Animation<TextureRegion> getAttackLunge() {
        return attackLunge;
    }

    public Animation<TextureRegion> getDeath() {
        return death;
    }

    public ArrayList<Texture> getLoadedTextures() {
        return loadedTextures;
    }

    public void dispose() {
        for (Texture texture : loadedTextures) {
            texture.dispose();
        }
        loadedTextures.clear();
    }
}
