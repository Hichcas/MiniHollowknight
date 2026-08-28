package Controller;

import Model.LevelData;
import Model.LevelData.MusicZone;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import java.util.ArrayList;
import java.util.List;

public class LevelLoader {

    private static final float TILE_SIZE = 16f;
    private static final String COLLISION_LAYER_NAME = "platform1";
    private static final String SPAWN_OBJECT_NAME = "SpawnPlace";

    public LevelData load(String mapPath, World world) {
        TiledMap map = new TmxMapLoader().load(mapPath);

        dumpMapContents(map);

        buildMapPhysics(map, world);

        Vector2 spawnPoint = extractSpawnPoint(map);
        Rectangle arenaBounds = extractArenaBounds(map);
        String musicPath = extractDefaultMusicPath(map);

        List<Rectangle> deadlyZones = extractDeadlyZones(map);
        List<Vector2> safeSpawnPoints = extractSafeSpawnPoints(map, spawnPoint);
        List<MusicZone> musicZones = extractMusicZones(map);

        return new LevelData(map, spawnPoint, arenaBounds, musicPath, deadlyZones, safeSpawnPoints, musicZones);
    }

    private void dumpMapContents(TiledMap map) {
        for (MapLayer layer : map.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                String type = obj.getClass().getSimpleName();
                String extra = "";
                if (obj instanceof RectangleMapObject) {
                    Rectangle r = ((RectangleMapObject) obj).getRectangle();
                    extra = String.format(" rect=[x=%.1f y=%.1f w=%.1f h=%.1f]",
                        r.x, r.y, r.width, r.height);
                }
            }
        }
    }

    public void buildMapPhysics(TiledMap map, World world) {
        MapLayer collisionLayer = map.getLayers().get(COLLISION_LAYER_NAME);
        if (collisionLayer == null) {
            return;
        }

        for (MapObject object : collisionLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) {
                continue;
            }

            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set(
                (rect.x + rect.width / 2f) / TILE_SIZE,
                (rect.y + rect.height / 2f) / TILE_SIZE
            );

            Body body = world.createBody(bodyDef);

            PolygonShape shape = new PolygonShape();
            shape.setAsBox(
                (rect.width / 2f) / TILE_SIZE,
                (rect.height / 2f) / TILE_SIZE
            );

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.friction = 0.5f;

            boolean isDeadly = hasBooleanProperty(object, "isDeadly", false);

            Fixture fixture = body.createFixture(fixtureDef);
            fixture.setUserData(isDeadly ? "DEADLY" : "GROUND");

            shape.dispose();
        }
    }

    public Body createKnightBody(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.3f, 0.45f);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1f;
        fixture.friction = 0.2f;

        Fixture playerFixture = body.createFixture(fixture);
        playerFixture.setUserData("PLAYER");

        shape.dispose();

        float sensorHalfWidth = 0.06f;
        float sensorHalfHeight = 0.5f;
        float sensorOffsetX = 0.3f + sensorHalfWidth;

        PolygonShape leftSensorShape = new PolygonShape();
        leftSensorShape.setAsBox(sensorHalfWidth, sensorHalfHeight, new Vector2(-sensorOffsetX, 0f), 0f);
        FixtureDef leftSensorDef = new FixtureDef();
        leftSensorDef.shape = leftSensorShape;
        leftSensorDef.isSensor = true;
        Fixture leftSensor = body.createFixture(leftSensorDef);
        leftSensor.setUserData("PLAYER_WALL_LEFT");
        leftSensorShape.dispose();

        PolygonShape rightSensorShape = new PolygonShape();
        rightSensorShape.setAsBox(sensorHalfWidth, sensorHalfHeight, new Vector2(sensorOffsetX, 0f), 0f);
        FixtureDef rightSensorDef = new FixtureDef();
        rightSensorDef.shape = rightSensorShape;
        rightSensorDef.isSensor = true;
        Fixture rightSensor = body.createFixture(rightSensorDef);
        rightSensor.setUserData("PLAYER_WALL_RIGHT");
        rightSensorShape.dispose();

        return body;
    }

    public Body createZoteBody(World world, float x, float y) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = true;

        Body body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(0.4f, 0.6f);

        FixtureDef fixture = new FixtureDef();
        fixture.shape = shape;
        fixture.density = 1f;
        fixture.friction = 0.6f;

        Fixture zoteFixture = body.createFixture(fixture);
        zoteFixture.setUserData("ZOTE");

        shape.dispose();
        return body;
    }

    private Vector2 extractSpawnPoint(TiledMap map) {
        Vector2 spawn = new Vector2(5f, 8f);

        for (MapLayer layer : map.getLayers()) {
            MapObject obj = layer.getObjects().get(SPAWN_OBJECT_NAME);
            if (obj instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                spawn.set(rect.x / TILE_SIZE, rect.y / TILE_SIZE);
                return spawn;
            }
        }

        String normalizedTarget = SPAWN_OBJECT_NAME.replaceAll("\\s+", "").toLowerCase();
        for (MapLayer layer : map.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if (!(obj instanceof RectangleMapObject)) {
                    continue;
                }
                String name = obj.getName();
                if (name != null && name.replaceAll("\\s+", "").toLowerCase().equals(normalizedTarget)) {
                    Rectangle rect = ((RectangleMapObject) obj).getRectangle();
                    spawn.set(rect.x / TILE_SIZE, rect.y / TILE_SIZE);
                    return spawn;
                }
            }
        }
        for (MapLayer layer : map.getLayers()) {
            String layerName = layer.getName();
            if (layerName == null
                || !layerName.replaceAll("\\s+", "").toLowerCase().equals(normalizedTarget)) {
                continue;
            }

            RectangleMapObject onlyRectangle = null;
            int rectangleCount = 0;
            for (MapObject obj : layer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    onlyRectangle = (RectangleMapObject) obj;
                    rectangleCount++;
                }
            }
            if (rectangleCount == 1) {
                Rectangle rect = onlyRectangle.getRectangle();
                spawn.set(rect.x / TILE_SIZE, rect.y / TILE_SIZE);
                return spawn;
            }
        }
        return spawn;
    }

    private Rectangle extractArenaBounds(TiledMap map) {
        Rectangle bounds = new Rectangle();
        Rectangle found = findRectangleObjectByName(map, "BossArenaBounds");
        String matchedName = "BossArenaBounds";

        if (found == null) {
            found = findRectangleObjectByName(map, "ArenaBounds");
            matchedName = "ArenaBounds";
        }

        if (found != null) {
            bounds.set(found.x / TILE_SIZE, found.y / TILE_SIZE,
                found.width / TILE_SIZE, found.height / TILE_SIZE);
        }

        return bounds;
    }

    public Vector2 getZoteSpawnPoint(TiledMap map) {
        Vector2 spawn = new Vector2(5f, 8f);
        Rectangle rect = findRectangleObjectByName(map, "ZoteSpawn");
        if (rect != null) {
            spawn.set(rect.x / TILE_SIZE, rect.y / TILE_SIZE);
        }
        return spawn;
    }

    private String extractDefaultMusicPath(TiledMap map) {
        Object value = map.getProperties().get("musicPath");
        if (value instanceof String) {
            String path = ((String) value).trim();
            if (!path.isEmpty()) {
                return path;
            }
        }
        Object alt = map.getProperties().get("music");
        if (alt instanceof String) {
            String path = ((String) alt).trim();
            if (!path.isEmpty()) {
                return path;
            }
        }
        return null;
    }

    private List<Rectangle> extractDeadlyZones(TiledMap map) {
        ArrayList<Rectangle> deadlyZones = new ArrayList<>();
        MapLayer collisionLayer = map.getLayers().get(COLLISION_LAYER_NAME);
        if (collisionLayer == null) {
            return deadlyZones;
        }

        for (MapObject object : collisionLayer.getObjects()) {
            if (!(object instanceof RectangleMapObject)) {
                continue;
            }
            if (!hasBooleanProperty(object, "isDeadly", false)) {
                continue;
            }
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            deadlyZones.add(new Rectangle(
                rect.x / TILE_SIZE,
                rect.y / TILE_SIZE,
                rect.width / TILE_SIZE,
                rect.height / TILE_SIZE
            ));
        }
        return deadlyZones;
    }

    private List<Vector2> extractSafeSpawnPoints(TiledMap map, Vector2 fallbackSpawn) {
        ArrayList<Vector2> safeSpawnPoints = new ArrayList<>();

        for (MapLayer layer : map.getLayers()) {
            String normalizedLayerName = normalizeName(layer.getName());
            boolean layerSuggestsSafeSpawn = normalizedLayerName.contains("safe");

            for (MapObject object : layer.getObjects()) {
                if (!(object instanceof RectangleMapObject)) {
                    continue;
                }

                boolean objectSuggestsSafeSpawn = layerSuggestsSafeSpawn
                    || hasBooleanProperty(object, "safePlace", false);

                if (!objectSuggestsSafeSpawn) {
                    continue;
                }

                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                Vector2 spawn = extractVectorFromObject(object, rect, fallbackSpawn);
                safeSpawnPoints.add(spawn);
            }
        }

        if (safeSpawnPoints.isEmpty()) {
            safeSpawnPoints.add(new Vector2(fallbackSpawn));
        }

        return safeSpawnPoints;
    }

    private List<MusicZone> extractMusicZones(TiledMap map) {
        ArrayList<MusicZone> musicZones = new ArrayList<>();

        for (MapLayer layer : map.getLayers()) {
            String normalizedLayerName = normalizeName(layer.getName());
            boolean layerSuggestsMusic = normalizedLayerName.contains("music")
                || normalizedLayerName.contains("room");

            for (MapObject object : layer.getObjects()) {
                if (!(object instanceof RectangleMapObject)) {
                    continue;
                }

                String musicPath = getStringProperty(object, "musicPath");
                if (musicPath == null || musicPath.isEmpty()) {
                    musicPath = getStringProperty(object, "track");
                }
                if (musicPath == null || musicPath.isEmpty()) {
                    continue;
                }

                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                if (!layerSuggestsMusic && getStringProperty(object, "musicPath") == null && getStringProperty(object, "track") == null) {
                    continue;
                }

                int priority = getIntProperty(object, "priority", 0);
                String id = object.getName();
                if (id == null || id.trim().isEmpty()) {
                    id = layer.getName();
                }

                musicZones.add(new MusicZone(id, new Rectangle(
                    rect.x / TILE_SIZE,
                    rect.y / TILE_SIZE,
                    rect.width / TILE_SIZE,
                    rect.height / TILE_SIZE
                ), musicPath, priority));
            }
        }

        return musicZones;
    }

    private Vector2 extractVectorFromObject(MapObject object, Rectangle rect, Vector2 fallbackSpawn) {
        float x = getFloatProperty(object, "spawnX", Float.NaN);
        if (Float.isNaN(x)) {
            x = getFloatProperty(object, "respawnX", Float.NaN);
        }
        float y = getFloatProperty(object, "spawnY", Float.NaN);
        if (Float.isNaN(y)) {
            y = getFloatProperty(object, "respawnY", Float.NaN);
        }

        if (Float.isNaN(x) || Float.isNaN(y)) {
            return new Vector2(
                (rect.x + rect.width / 2f) / TILE_SIZE,
                (rect.y + rect.height / 2f) / TILE_SIZE
            );
        }

        return new Vector2(x / TILE_SIZE, y / TILE_SIZE);
    }

    private String getStringProperty(MapObject object, String key) {
        Object value = object.getProperties().get(key);
        if (value instanceof String) {
            String text = ((String) value).trim();
            return text.isEmpty() ? null : text;
        }
        return null;
    }

    private float getFloatProperty(MapObject object, String key, float defaultValue) {
        Object value = object.getProperties().get(key);
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            try {
                return Float.parseFloat(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private int getIntProperty(MapObject object, String key, int defaultValue) {
        Object value = object.getProperties().get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String normalizeName(String name) {
        return name == null ? "" : name.replaceAll("\\s+", "").toLowerCase();
    }

    private Rectangle findRectangleObjectByName(TiledMap map, String objectName) {
        for (MapLayer layer : map.getLayers()) {
            MapObject obj = layer.getObjects().get(objectName);
            if (obj instanceof RectangleMapObject) {
                return ((RectangleMapObject) obj).getRectangle();
            }
        }

        String normalizedTarget = objectName.replaceAll("\\s+", "").toLowerCase();
        for (MapLayer layer : map.getLayers()) {
            for (MapObject obj : layer.getObjects()) {
                if (!(obj instanceof RectangleMapObject)) {
                    continue;
                }
                String name = obj.getName();
                if (name != null && name.replaceAll("\\s+", "").toLowerCase().equals(normalizedTarget)) {
                    return ((RectangleMapObject) obj).getRectangle();
                }
            }
        }

        for (MapLayer layer : map.getLayers()) {
            String layerName = layer.getName();
            if (layerName == null
                || !layerName.replaceAll("\\s+", "").toLowerCase().equals(normalizedTarget)) {
                continue;
            }

            RectangleMapObject onlyRectangle = null;
            int rectangleCount = 0;
            for (MapObject obj : layer.getObjects()) {
                if (obj instanceof RectangleMapObject) {
                    onlyRectangle = (RectangleMapObject) obj;
                    rectangleCount++;
                }
            }
            if (rectangleCount == 1) {
                return onlyRectangle.getRectangle();
            }
        }

        return null;
    }

    private boolean hasBooleanProperty(MapObject object, String key, boolean defaultValue) {
        Object value = object.getProperties().get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }
}
