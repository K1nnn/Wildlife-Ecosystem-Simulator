package com.ecosystem.sim.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class MapManager {
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private float unitScale = 1.0f; // Unit scale cho renderer
    private float cameraZoom = 1.0f; // Zoom level của camera
    private float minZoom = 0.5f;
    private float maxZoom = 3.0f;
    private static final float mapWidth = 800f;  // 50 tiles × 16px
    private static final float mapHeight = 800f; // 50 tiles × 16px

    private static final java.util.Set<Integer> WATER_TILE_IDS = new java.util.HashSet<>(java.util.Arrays.asList(
        3, 4, 5, 59, 60, 61, 115, 116, 117, 398, 455, 512, 513, 514, 568, 569, 570, 624, 625, 626
    ));

    public MapManager(String mapPath, OrthographicCamera camera) {
        this.map = new TmxMapLoader().load(mapPath);
        this.renderer = new OrthogonalTiledMapRenderer(map, unitScale);
        this.camera = camera;
    }

    public void render() {
        renderer.setView(camera);
        renderer.render();
    }

    /**
     * Kiểm tra xem một ô gạch (tile) cụ thể có phải là vật cản không
     */
    public boolean isTileObstacle(int tileX, int tileY) {
        if (tileX < 0 || tileX >= 50 || tileY < 0 || tileY >= 50) {
            return true; // Biên bản đồ là vật cản
        }

        // Lấy lớp "Tile Layer 2" (vật cản cây cối, đá vẽ sẵn trên map)
        TiledMapTileLayer obstacleLayer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 2");
        if (obstacleLayer != null && obstacleLayer.getCell(tileX, tileY) != null) {
            return true;
        }

        // Kiểm tra nước vẽ ở Tile Layer 1 (các con vật không lội nước)
        TiledMapTileLayer layer1 = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        if (layer1 != null) {
            TiledMapTileLayer.Cell cell = layer1.getCell(tileX, tileY);
            if (cell != null && cell.getTile() != null) {
                int id = cell.getTile().getId();
                if (WATER_TILE_IDS.contains(id)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Kiểm tra xem một vùng kích thước width x height có bị va chạm với vật cản nào không
     * Tránh hoàn toàn lỗi sói/thỏ đi dính góc hoặc kẹt vào cây
     */
    public boolean isAreaObstacle(float worldX, float worldY, float width, float height) {
        int minTileX = (int) (worldX / (16 * unitScale));
        int maxTileX = (int) ((worldX + width - 0.1f) / (16 * unitScale));
        int minTileY = (int) (worldY / (16 * unitScale));
        int maxTileY = (int) ((worldY + height - 0.1f) / (16 * unitScale));

        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int ty = minTileY; ty <= maxTileY; ty++) {
                if (isTileObstacle(tx, ty)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem một ô gạch có phải là vật cản không, bỏ qua kiểm tra nước
     */
    public boolean isTileObstacleIgnoreWater(int tileX, int tileY) {
        if (tileX < 0 || tileX >= 50 || tileY < 0 || tileY >= 50) {
            return true; // Biên bản đồ vẫn là vật cản
        }

        // Chỉ kiểm tra lớp "Tile Layer 2" (vật cản cây cối, đá vẽ sẵn trên map)
        TiledMapTileLayer obstacleLayer = (TiledMapTileLayer) map.getLayers().get("Tile Layer 2");
        if (obstacleLayer != null && obstacleLayer.getCell(tileX, tileY) != null) {
            return true;
        }

        return false;
    }

    /**
     * Kiểm tra xem một vùng có bị va chạm vật cản không, bỏ qua kiểm tra nước
     */
    public boolean isAreaObstacleIgnoreWater(float worldX, float worldY, float width, float height) {
        int minTileX = (int) (worldX / (16 * unitScale));
        int maxTileX = (int) ((worldX + width - 0.1f) / (16 * unitScale));
        int minTileY = (int) (worldY / (16 * unitScale));
        int maxTileY = (int) ((worldY + height - 0.1f) / (16 * unitScale));

        for (int tx = minTileX; tx <= maxTileX; tx++) {
            for (int ty = minTileY; ty <= maxTileY; ty++) {
                if (isTileObstacleIgnoreWater(tx, ty)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem một tọa độ có phải là vật cản không (tương thích ngược)
     */
    public boolean isObstacle(float worldX, float worldY) {
        int tileX = (int) (worldX / (16 * unitScale));
        int tileY = (int) (worldY / (16 * unitScale));
        return isTileObstacle(tileX, tileY);
    }

    /**
     * Kiểm tra xem một tọa độ có phải là nước không
     */
    public boolean isWater(float worldX, float worldY) {
        int tileX = (int) (worldX / (16 * unitScale));
        int tileY = (int) (worldY / (16 * unitScale));

        TiledMapTileLayer layer1 = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        if (layer1 != null) {
            TiledMapTileLayer.Cell cell = layer1.getCell(tileX, tileY);
            if (cell != null && cell.getTile() != null) {
                int id = cell.getTile().getId();
                if (WATER_TILE_IDS.contains(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem một tọa độ có phải là cỏ (ID 6) không
     */
    public boolean isGrass(float worldX, float worldY) {
        int tileX = (int) (worldX / (16 * unitScale));
        int tileY = (int) (worldY / (16 * unitScale));

        TiledMapTileLayer layer1 = (TiledMapTileLayer) map.getLayers().get("Tile Layer 1");
        if (layer1 != null) {
            TiledMapTileLayer.Cell cell = layer1.getCell(tileX, tileY);
            if (cell != null && cell.getTile() != null) {
                int id = cell.getTile().getId();
                return id == 6; // Cỏ có ID là 6 trong tileset
            }
        }
        return false;
    }

    /**
     * Kiểm tra xem một vị trí có phải là land hợp lệ (phải trên cỏ, không có vật cản)
     */
    public boolean isValidSpawnLocation(float worldX, float worldY) {
        return isValidSpawnLocation(worldX, worldY, 14f, 14f);
    }

    /**
     * Kiểm tra xem một thực thể với kích thước cụ thể có thể spawn tại vị trí đó không
     */
    public boolean isValidSpawnLocation(float worldX, float worldY, float width, float height) {
        // Kiểm tra bounds
        if (worldX < 0 || worldX + width >= mapWidth || worldY < 0 || worldY + height >= mapHeight) {
            return false;
        }
        
        // Kiểm tra va chạm toàn diện qua isAreaObstacle để ngăn chặn đè nước hoặc đè cây vẽ sẵn
        if (isAreaObstacle(worldX, worldY, width, height)) {
            return false;
        }
        
        // Kiểm tra 4 góc của thực thể để đảm bảo nằm trọn trên đất cỏ
        float[] xs = {worldX, worldX + width};
        float[] ys = {worldY, worldY + height};

        for (float x : xs) {
            for (float y : ys) {
                if (!isGrass(x, y)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Tìm vị trí spawn ngẫu nhiên hợp lệ trên cỏ cho thực thể kích thước mặc định (14x14)
     */
    public Vector2 findRandomSpawnLocation() {
        return findRandomSpawnLocation(14f, 14f);
    }

    /**
     * Tìm vị trí spawn ngẫu nhiên hợp lệ trên cỏ cho thực thể có kích thước cụ thể
     */
    public Vector2 findRandomSpawnLocation(float width, float height) {
        Vector2 spawnPos = new Vector2();
        int maxAttempts = 100;
        int attempts = 0;
        
        // Thử tìm vị trí hợp lệ tối đa 100 lần
        while (attempts < maxAttempts) {
            float randomX = MathUtils.random(0, mapWidth - width);
            float randomY = MathUtils.random(0, mapHeight - height);
            
            if (isValidSpawnLocation(randomX, randomY, width, height)) {
                spawnPos.set(randomX, randomY);
                return spawnPos;
            }
            
            attempts++;
        }
        
        // Fallback: Nếu không tìm được thì trả về vị trí gốc ngẫu nhiên trên vùng cỏ an toàn phía Bắc (hàng 38 đến 48 trong LibGDX)
        float safeX = MathUtils.random(16, mapWidth - 32);
        float safeY = MathUtils.random(38 * 16, 48 * 16);
        return new Vector2(safeX, safeY);
    }

    /**
     * Điều chỉnh mức zoom với camera
     */
    public void adjustZoom(float zoomDelta) {
        cameraZoom += zoomDelta;
        if (cameraZoom < minZoom) cameraZoom = minZoom;
        if (cameraZoom > maxZoom) cameraZoom = maxZoom;
        
        camera.zoom = cameraZoom;
        camera.update();
    }

    /**
     * Đặt mức zoom cụ thể
     */
    public void setZoom(float zoom) {
        cameraZoom = zoom;
        if (cameraZoom < minZoom) cameraZoom = minZoom;
        if (cameraZoom > maxZoom) cameraZoom = maxZoom;
        
        camera.zoom = cameraZoom;
        camera.update();
    }

    /**
     * Lấy mức zoom hiện tại
     */
    public float getZoom() {
        return cameraZoom;
    }

    public void dispose() {
        map.dispose();
        renderer.dispose();
    }
}
