package Controller;

import Model.Enums.KnightState;
import Model.HowlingWraithsBurst;
import Model.Knight;
import Model.VengefulSpiritBolt;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import java.util.ArrayList;

public class SpellController {
    private final World world;
    private final Animation<TextureRegion> vengefulSpiritAnimation;
    private final Animation<TextureRegion> howlingWraithsAnimation;

    private final ArrayList<VengefulSpiritBolt> bolts = new ArrayList<>();
    private final ArrayList<HowlingWraithsBurst> bursts = new ArrayList<>();

    private int lastVengefulSequenceSpawned = -1;
    private int lastHowlingSequenceSpawned = -1;

    public SpellController(World world,
                           Animation<TextureRegion> vengefulSpiritAnimation,
                           Animation<TextureRegion> howlingWraithsAnimation) {
        this.world = world;
        this.vengefulSpiritAnimation = vengefulSpiritAnimation;
        this.howlingWraithsAnimation = howlingWraithsAnimation;
    }

    public void update(float delta, Knight knight) {
        handleVengefulSpiritSpawn(knight);
        handleHowlingWraithsSpawn(knight);

        for (int i = bolts.size() - 1; i >= 0; i--) {
            VengefulSpiritBolt bolt = bolts.get(i);
            bolt.update(delta, world);
            if (bolt.isRemovePending()) {
                bolts.remove(i);
            }
        }

        for (int i = bursts.size() - 1; i >= 0; i--) {
            HowlingWraithsBurst burst = bursts.get(i);
            burst.update(delta);
            if (burst.isRemovePending()) {
                bursts.remove(i);
            }
        }
    }

    private void handleVengefulSpiritSpawn(Knight knight) {
        if (knight.getState() != KnightState.VENGEFULSPIRIT) {
            lastVengefulSequenceSpawned = -1;
            return;
        }

        int seq = knight.getVengefulSpiritSequence();
        if (seq == lastVengefulSequenceSpawned) {
            return;
        }
        lastVengefulSequenceSpawned = seq;

        Vector2 pos = knight.getBody().getPosition();
        boolean facingRight = knight.getFacing() > 0f;
        float boltWidth = 2.4f;
        float spawnX = facingRight ? pos.x + 0.5f : pos.x - 0.5f - boltWidth;
        float spawnY = pos.y - 0.35f;

        bolts.add(new VengefulSpiritBolt(
            vengefulSpiritAnimation, spawnX, spawnY, facingRight, knight.getVengefulSpiritDamage()
        ));
    }

    private void handleHowlingWraithsSpawn(Knight knight) {
        if (knight.getState() != KnightState.HOWLINGWRAITHS) {
            lastHowlingSequenceSpawned = -1;
            return;
        }

        int seq = knight.getHowlingWraithsSequence();
        if (seq == lastHowlingSequenceSpawned) {
            return;
        }
        lastHowlingSequenceSpawned = seq;

        Vector2 pos = knight.getBody().getPosition();
        float centerX = pos.x;
        float bottomY = pos.y + knight.getKnightHeight() / 2f;

        bursts.add(new HowlingWraithsBurst(
            howlingWraithsAnimation, centerX, bottomY, knight.getHowlingWraithsDamagePerTick()
        ));
    }

    public void render(Batch batch) {
        renderBolts(batch);
        renderBursts(batch);
    }

    public void renderBolts(Batch batch) {
        for (VengefulSpiritBolt bolt : bolts) {
            bolt.render(batch);
        }
    }

    public void renderBursts(Batch batch) {
        for (HowlingWraithsBurst burst : bursts) {
            burst.render(batch);
        }
    }

    public ArrayList<VengefulSpiritBolt> getBolts() {
        return bolts;
    }

    public ArrayList<HowlingWraithsBurst> getBursts() {
        return bursts;
    }
}
