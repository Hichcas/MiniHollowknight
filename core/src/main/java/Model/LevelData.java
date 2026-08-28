package Model;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LevelData {
    public static final class MusicZone {
        private final String id;
        private final Rectangle bounds;
        private final String musicPath;
        private final int priority;

        public MusicZone(String id, Rectangle bounds, String musicPath, int priority) {
            this.id = id == null ? "" : id;
            this.bounds = new Rectangle(bounds);
            this.musicPath = musicPath == null ? "" : musicPath.trim();
            this.priority = priority;
        }

        public String getId() {
            return id;
        }

        public Rectangle getBounds() {
            return new Rectangle(bounds);
        }

        public String getMusicPath() {
            return musicPath;
        }

        public int getPriority() {
            return priority;
        }

        public boolean contains(Vector2 point) {
            return point != null && bounds.contains(point.x, point.y);
        }
    }

    private final TiledMap map;
    private final Vector2 spawnPoint;
    private final Rectangle arenaBounds;
    private final String musicPath;
    private final List<Rectangle> deadlyZones;
    private final List<Vector2> safeSpawnPoints;
    private final List<MusicZone> musicZones;

    public LevelData(
        TiledMap map,
        Vector2 spawnPoint,
        Rectangle arenaBounds,
        String musicPath,
        List<Rectangle> deadlyZones,
        List<Vector2> safeSpawnPoints,
        List<MusicZone> musicZones
    ) {
        this.map = map;
        this.spawnPoint = new Vector2(spawnPoint);
        this.arenaBounds = new Rectangle(arenaBounds);
        this.musicPath = musicPath;
        this.deadlyZones = copyRectangles(deadlyZones);
        this.safeSpawnPoints = copyVectors(safeSpawnPoints);
        this.musicZones = copyMusicZones(musicZones);
    }

    private static List<Rectangle> copyRectangles(List<Rectangle> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Rectangle> copy = new ArrayList<>(source.size());
        for (Rectangle rect : source) {
            if (rect != null) {
                copy.add(new Rectangle(rect));
            }
        }
        return Collections.unmodifiableList(copy);
    }

    private static List<Vector2> copyVectors(List<Vector2> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Vector2> copy = new ArrayList<>(source.size());
        for (Vector2 point : source) {
            if (point != null) {
                copy.add(new Vector2(point));
            }
        }
        return Collections.unmodifiableList(copy);
    }

    private static List<MusicZone> copyMusicZones(List<MusicZone> source) {
        if (source == null || source.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<MusicZone> copy = new ArrayList<>(source.size());
        for (MusicZone zone : source) {
            if (zone != null) {
                copy.add(zone);
            }
        }
        return Collections.unmodifiableList(copy);
    }

    public TiledMap getMap() {
        return map;
    }

    public Vector2 getSpawnPoint() {
        return new Vector2(spawnPoint);
    }

    public Rectangle getArenaBounds() {
        return new Rectangle(arenaBounds);
    }

    public String getMusicPath() {
        return musicPath;
    }

    public List<Rectangle> getDeadlyZones() {
        return deadlyZones;
    }

    public List<Vector2> getSafeSpawnPoints() {
        return safeSpawnPoints;
    }

    public List<MusicZone> getMusicZones() {
        return musicZones;
    }

    public Vector2 findNearestSafeSpawn(Vector2 from) {
        if (safeSpawnPoints.isEmpty()) {
            return new Vector2(spawnPoint);
        }

        Vector2 origin = from == null ? spawnPoint : from;
        Vector2 best = safeSpawnPoints.get(0);
        float bestDst2 = origin.dst2(best);

        for (int i = 1; i < safeSpawnPoints.size(); i++) {
            Vector2 candidate = safeSpawnPoints.get(i);
            float dst2 = origin.dst2(candidate);
            if (dst2 < bestDst2) {
                bestDst2 = dst2;
                best = candidate;
            }
        }

        return new Vector2(best);
    }

    public String getMusicPathForPosition(Vector2 position) {
        if (position == null || musicZones.isEmpty()) {
            return musicPath;
        }
        MusicZone bestMatch = null;
        for (MusicZone zone : musicZones) {
            if (zone.contains(position)) {
                if (bestMatch == null || zone.getPriority() > bestMatch.getPriority()) {
                    bestMatch = zone;
                }
            }
        }
        if (bestMatch != null && !bestMatch.getMusicPath().isEmpty()) {
            return bestMatch.getMusicPath();
        }

        return musicPath;

    }
}
