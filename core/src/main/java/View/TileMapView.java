package View;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import java.util.ArrayList;

public class TileMapView {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public TileMapView(TiledMap map, OrthogonalTiledMapRenderer mapRenderer) {
        this.map = map;
        this.mapRenderer = mapRenderer;
    }

    public void setView(OrthographicCamera camera) {
        if (camera != null && mapRenderer != null) {
            mapRenderer.setView(camera);
        }
    }

    public void renderBelowEntities() {
        renderLayers(false);
    }

    public void renderAboveEntities() {
        renderLayers(true);
    }

    private void renderLayers(boolean aboveEntities) {
        if (map == null || mapRenderer == null) {
            return;
        }

        ArrayList<Integer> indices = new ArrayList<>();
        int layerIndex = 0;

        for (MapLayer layer : map.getLayers()) {
            if (layer instanceof TiledMapTileLayer) {
                if (isLayerAboveEntities(layer) == aboveEntities) {
                    indices.add(layerIndex);
                }
            }
            layerIndex++;
        }

        if (indices.isEmpty()) {
            return;
        }

        int[] layerArray = new int[indices.size()];
        for (int i = 0; i < indices.size(); i++) {
            layerArray[i] = indices.get(i);
        }

        mapRenderer.render(layerArray);
    }

    private boolean isLayerAboveEntities(MapLayer layer) {
        Object value = layer.getProperties().get("drawAboveEntities");
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return false;
    }

    public void dispose() {
        if (mapRenderer != null) {
            mapRenderer.dispose();
        }
        if (map != null) {
            map.dispose();
        }
    }
}
