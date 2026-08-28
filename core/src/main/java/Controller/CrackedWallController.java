package Controller;

import Model.CrackedWall;
import Model.Enums.CharmState;
import Model.Knight;
import Model.SecretRoom;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrackedWallController {

    private static final float TILE_SIZE = 16f;
    private static final float HIT_DEBOUNCE = 0.5f;

    private final List<CrackedWall> walls = new ArrayList<>();
    private final Map<String, SecretRoom> secretRooms = new HashMap<>();
    private final List<CharmPickup> charmPickups = new ArrayList<>();

    public static final float PICKUP_ANIM_DURATION = 2f;
    public static final float PICKUP_FADE_DURATION = 0.6f;

    public static class CharmPickup {
        public final String id;
        public final Rectangle bounds;
        public final CharmState charm;
        public final String requiredRoomId;
        public boolean collected = false;
        public boolean collecting = false;
        public boolean playerNearby = false;
        public float fadeTimer = 0f;
        public float idleTimer = 0f;

        public CharmPickup(String id, Rectangle bounds, CharmState charm, String requiredRoomId) {
            this.id = id;
            this.bounds = bounds;
            this.charm = charm;
            this.requiredRoomId = requiredRoomId;
        }

        public float getFadeProgress() {
            if (!collecting) return 0f;
            return Math.min(1f, fadeTimer / PICKUP_FADE_DURATION);
        }
    }

    public List<CrackedWall> getWalls() {
        return walls;
    }

    public Map<String, SecretRoom> getSecretRooms() {
        return secretRooms;
    }

    public List<CharmPickup> getCharmPickups() {
        return charmPickups;
    }

    private static boolean readFlipProperty(MapProperties props) {
        String[] candidateKeys = {"flipX", "flipx", "FlipX", "flip_x", "mirror", "mirrorX", "flip"};
        for (String key : candidateKeys) {
            if (props.containsKey(key)) {
                return Boolean.parseBoolean(String.valueOf(props.get(key)));
            }
        }
        java.util.Iterator<String> keys = props.getKeys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (key.toLowerCase().contains("flip")) {
                return Boolean.parseBoolean(String.valueOf(props.get(key)));
            }
        }
        return false;
    }

    private static String normalizeId(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public void load(TiledMap map, World world) {
        loadSecretRooms(map);
        loadCrackedWalls(map, world);
        loadCharmPickups(map);
    }

    private void loadSecretRooms(TiledMap map) {
        MapLayer layer = map.getLayers().get("SecretRooms");
        if (layer == null) return;

        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) continue;
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            MapProperties props = obj.getProperties();
            String id = props.containsKey("id") ? normalizeId(String.valueOf(props.get("id"))) : normalizeId(obj.getName());
            if (id == null) continue;

            Rectangle worldRect = new Rectangle(
                rect.x / TILE_SIZE, rect.y / TILE_SIZE,
                rect.width / TILE_SIZE, rect.height / TILE_SIZE
            );
            secretRooms.put(id, new SecretRoom(id, worldRect));
        }
    }

    private void loadCrackedWalls(TiledMap map, World world) {
        MapLayer layer = map.getLayers().get("CrackedWalls");
        if (layer == null) return;

        int autoId = 0;
        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) continue;
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            MapProperties props = obj.getProperties();

            String roomId = props.containsKey("room") ? normalizeId(String.valueOf(props.get("room"))) : null;
            String id = obj.getName() != null ? obj.getName() : ("crackedWall_" + (autoId++));
            boolean flipX = readFlipProperty(props);

            Rectangle worldRect = new Rectangle(
                rect.x / TILE_SIZE, rect.y / TILE_SIZE,
                rect.width / TILE_SIZE, rect.height / TILE_SIZE
            );

            CrackedWall wall = new CrackedWall(id, worldRect, roomId, flipX);
            createWallBody(wall, world);
            walls.add(wall);
        }
    }

    private void createWallBody(CrackedWall wall, World world) {
        Rectangle rect = wall.getBounds();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(rect.x + rect.width / 2f, rect.y + rect.height / 2f);
        Body body = world.createBody(bodyDef);
        body.setUserData(wall);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(rect.width / 2f, rect.height / 2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.5f;

        Fixture fixture = body.createFixture(fixtureDef);
        fixture.setUserData("CRACKED_WALL");
        shape.dispose();

        wall.setBody(body);
        wall.setFixture(fixture);
    }

    private void loadCharmPickups(TiledMap map) {
        MapLayer layer = map.getLayers().get("CharmPickups");
        if (layer == null) return;

        int autoId = 0;
        for (MapObject obj : layer.getObjects()) {
            if (!(obj instanceof RectangleMapObject)) continue;
            Rectangle rect = ((RectangleMapObject) obj).getRectangle();
            MapProperties props = obj.getProperties();

            String charmName = props.containsKey("charm") ? String.valueOf(props.get("charm")) : null;
            if (charmName == null) continue;

            CharmState charmState;
            try {
                charmState = CharmState.valueOf(charmName.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                continue;
            }

            String roomId = props.containsKey("room") ? normalizeId(String.valueOf(props.get("room"))) : null;
            String id = obj.getName() != null ? obj.getName() : ("charmPickup_" + (autoId++));

            Rectangle worldRect = new Rectangle(
                rect.x / TILE_SIZE, rect.y / TILE_SIZE,
                rect.width / TILE_SIZE, rect.height / TILE_SIZE
            );

            charmPickups.add(new CharmPickup(id, worldRect, charmState, roomId));
        }
    }

    public void update(float delta) {
        for (CrackedWall wall : walls) {
            if (wall.getHitCooldown() > 0f) {
                wall.setHitCooldown(wall.getHitCooldown() - delta);
            }
        }
        for (CharmPickup pickup : charmPickups) {
            if (pickup.collected) continue;
            if (!pickup.collecting) {
                pickup.idleTimer += delta;
            } else {
                pickup.fadeTimer += delta;
                if (pickup.fadeTimer >= PICKUP_FADE_DURATION) {
                    pickup.collected = true;
                }
            }
        }
    }

    public void handleKnightAttackHits(Knight knight, World world) {
        if (knight == null || knight.isDead() || !knight.isAttacking()) {
            return;
        }

        Rectangle attackBox = knight.getAttackBounds();

        for (CrackedWall wall : walls) {
            if (!wall.canBeHit()) continue;
            if (!attackBox.overlaps(wall.getBounds())) continue;

            wall.setHitCooldown(HIT_DEBOUNCE);
            boolean destroyedNow = wall.applyHit();

            if (destroyedNow) {
                destroyWallCollider(wall, world);
                revealLinkedRoom(wall);
            }
        }
    }

    private void destroyWallCollider(CrackedWall wall, World world) {

        if (wall.getBody() != null && wall.getFixture() != null) {
            wall.getBody().destroyFixture(wall.getFixture());
            wall.setFixture(null);
        }
    }

    private void revealLinkedRoom(CrackedWall wall) {
        if (wall.getRoomId() == null) return;
        SecretRoom room = secretRooms.get(wall.getRoomId());
        if (room != null) {
            room.reveal();
        }
    }

    public boolean isRoomRevealed(String roomId) {
        SecretRoom room = secretRooms.get(roomId);
        return room != null && room.isRevealed();
    }

    public void checkCharmPickups(Knight knight, CharmManager charmManager, AchievementManager achievementManager) {
        if (knight == null || knight.getBody() == null) return;

        Vector2 pos = knight.getBody().getPosition();
        Rectangle knightBox = new Rectangle(pos.x - 0.6f, pos.y - 0.7f, 1.2f, 1.4f);
        boolean interactKey = com.badlogic.gdx.Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.W);

        for (CharmPickup pickup : charmPickups) {
            if (pickup.collected || pickup.collecting) continue;
            if (pickup.requiredRoomId != null && !isRoomRevealed(pickup.requiredRoomId)) continue;

            boolean overlapping = knightBox.overlaps(pickup.bounds);
            pickup.playerNearby = overlapping;
            if (!overlapping || !interactKey) continue;

            pickup.collecting = true;
            pickup.fadeTimer = 0f;

            if (charmManager != null) {
                charmManager.setUnlocked(pickup.charm, true);
            }
            if (achievementManager != null) {
                achievementManager.unlock(AchievementManager.charmFoundId(pickup.charm));
            }
            knight.triggerItemPickupAnimation(PICKUP_ANIM_DURATION);
        }
    }
}
