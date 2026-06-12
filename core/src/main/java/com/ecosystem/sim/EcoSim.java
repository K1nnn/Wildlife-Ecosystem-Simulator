package com.ecosystem.sim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.ui.HUD;

/**
 * Lớp điều phối game chính của LibGDX.
 */
public class EcoSim extends ApplicationAdapter implements InputProcessor {
    private OrthographicCamera camera;
    private MapManager mapManager;
    private ShapeRenderer shapeRenderer;
    private com.ecosystem.sim.util.EntityManager entityManager;
    
    // Giao diện HUD và SpriteBatch
    private SpriteBatch batch;
    private HUD hud;

    // Camera dragging
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private boolean isDragging = false;
    
    // Spawn qua thời gian thay vì một lúc
    private float spawnTimer = 0;
    private float spawnInterval = 0.2f; // Spawn 1 entity mỗi 0.2 giây
    
    public static final int MAX_RABBITS = 80;
    public static final int MAX_DEERS = 40;
    public static final int MAX_ELEPHANTS = 10;
    public static final int MAX_WOLVES = 12;
    public static final int MAX_TIGERS = 6;

    // Các giới hạn tối thiểu (dự trữ cho tương lai - hiện không dùng cho spawn)
    // Có thể dùng sau này cho logic bảo vệ tuyệt chủng nâng cao
    public static final int MIN_RABBITS = 20;
    public static final int MIN_DEERS = 10;
    public static final int MIN_ELEPHANTS = 5;
    public static final int MIN_WOLVES = 6;
    public static final int MIN_TIGERS = 3;

    // Các hằng số giới hạn số lượng và xác suất mọc của Thực vật (Cỏ và Cây)
    // Tỉ lệ Cỏ luôn gấp 9 lần Cây ở tất cả các mùa
    public static final int MAX_GRASS_BREEDING = 540;
    public static final int MAX_TREES_BREEDING = 60;
    public static final float GRASS_SPAWN_CHANCE_BREEDING = 0.9f;
    public static final float TREE_SPAWN_CHANCE_BREEDING = 0.1f;

    public static final int MAX_GRASS_DROUGHT = 90;
    public static final int MAX_TREES_DROUGHT = 10;
    public static final float GRASS_SPAWN_CHANCE_DROUGHT = 0.45f;
    public static final float TREE_SPAWN_CHANCE_DROUGHT = 0.05f;


    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Bản đồ: 50x50 tiles, mỗi tile 16px => 800x800 pixels
        float mapCenterX = (50 * 16) / 2f; // 400
        float mapCenterY = (50 * 16) / 2f; // 400
        camera.position.set(mapCenterX, mapCenterY, 0);
        camera.zoom = 1.0f;
        camera.update();

        mapManager = new MapManager("ecosystem.tmx", camera);
        
        // --- KHỞI TẠO HỆ THỐNG RENDERING ---
        shapeRenderer = new ShapeRenderer();
        entityManager = new com.ecosystem.sim.util.EntityManager(mapManager);
        
        // Khởi tạo HUD & SpriteBatch
        batch = new SpriteBatch();
        hud = new HUD(entityManager);
        
        // Thiết lập bộ đa xử lý sự kiện: Stage của HUD sẽ đón sự kiện click trước, nếu click không trúng UI thì chuyển sang EcoSim xử lý drag camera.
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hud.getStage());
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
        
        com.ecosystem.sim.util.SoundManager.init();

        // --- KHỞI TẠO QUẦN THỂ BAN ĐẦU ---
        for (int i = 0; i < 180; i++) {
            com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation();
            entityManager.spawnGrass(pos.x, pos.y);
        }
        for (int i = 0; i < 20; i++) {
            com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation();
            entityManager.spawnTree(pos.x, pos.y);
        }
        for (int i = 0; i < MAX_RABBITS; i++) {
            com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
            entityManager.spawnRabbit(pos.x, pos.y);
        }
        for (int i = 0; i < MAX_DEERS; i++) {
            com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
            entityManager.spawnDeer(pos.x, pos.y);
        }
        for (int i = 0; i < MAX_ELEPHANTS; i++) {
            com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
            entityManager.spawnElephant(pos.x, pos.y);
        }
        // 3. Động vật ăn thịt (~50% của MAX)
        for (int i = 0; i < MAX_WOLVES; i++) {
            com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
            entityManager.spawnWolf(pos.x, pos.y);
        }
        for (int i = 0; i < MAX_TIGERS; i++) {
            com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
            entityManager.spawnTiger(pos.x, pos.y);
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.2f, 0.2f, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Áp dụng bounds checking cho camera
        applyBounds();

        camera.update();
        mapManager.render();

        float deltaTime = Gdx.graphics.getDeltaTime();
        
        // Lấy giới hạn tối đa và tỉ lệ mọc theo mùa
        int maxGrass = MAX_GRASS_BREEDING;
        int maxTrees = MAX_TREES_BREEDING;
        float grassSpawnChance = GRASS_SPAWN_CHANCE_BREEDING;
        float treeSpawnChance = TREE_SPAWN_CHANCE_BREEDING;
        
        if (com.ecosystem.sim.util.EntityManager.getCurrentSeason() == com.ecosystem.sim.util.Season.DROUGHT) {
            maxGrass = MAX_GRASS_DROUGHT;
            maxTrees = MAX_TREES_DROUGHT;
            grassSpawnChance = GRASS_SPAWN_CHANCE_DROUGHT;
            treeSpawnChance = TREE_SPAWN_CHANCE_DROUGHT;
        }
        
        // Time-sliced Spawning
        spawnTimer += deltaTime;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
            
            // Spawn động vật ăn cỏ (Rabbit) 
            if (entityManager.getRabbitCount() < MIN_RABBITS) {
                com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
                entityManager.spawnRabbit(pos.x, pos.y);
            }

            // Spawn động vật ăn cỏ (Deer) 
            if (entityManager.getDeerCount() < MIN_DEERS) {
                com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
                entityManager.spawnDeer(pos.x, pos.y);
            }

            // Spawn động vật ăn cỏ (Elephant) 
            if (entityManager.getElephantCount() < MIN_ELEPHANTS) {
                com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
                entityManager.spawnElephant(pos.x, pos.y);
            }
            
            // Spawn động vật ăn thịt (Wolf) 
            if (entityManager.getWolfCount() < MIN_WOLVES) {
                com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
                entityManager.spawnWolf(pos.x, pos.y);
            }

            // Spawn động vật ăn thịt (Tiger) 
            if (entityManager.getTigerCount() < MIN_TIGERS) {
                com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation(12, 12);
                entityManager.spawnTiger(pos.x, pos.y);
            }
            
            // Spawn cỏ ngẫu nhiên liên tục theo điều kiện mùa
            if (entityManager.getGrassCount() < maxGrass && com.badlogic.gdx.math.MathUtils.random() < grassSpawnChance) {
                com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation();
                entityManager.spawnGrass(pos.x, pos.y);
            }
            
            // Spawn cây ngẫu nhiên liên tục theo điều kiện mùa
            if (entityManager.getTreeCount() < maxTrees && com.badlogic.gdx.math.MathUtils.random() < treeSpawnChance) {
                com.badlogic.gdx.math.Vector2 pos = mapManager.findRandomSpawnLocation();
                entityManager.spawnTree(pos.x, pos.y);
            }
        }
        
        // --- CẬP NHẬT VÀ VẼ CÁC ĐỘNG VẬT ---
        
        // Cho phép các con vật suy nghĩ và di chuyển
        entityManager.update(deltaTime); 
        
        // Thiết lập ShapeRenderer vẽ tại vị trí camera
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Vẽ các con vật và thực vật lên bản đồ
        entityManager.render(shapeRenderer);
        
        shapeRenderer.end();
        
        // --- VẼ HUD TRÊN CÙNG ---
        if (hud != null) {
            hud.render(batch);
        }
    }

    /**
     * Giới hạn camera không đi ra ngoài bản đồ
     */
    private void applyBounds() {
        // Bản đồ: 50x50 tiles × 16 pixels = 800×800
        float mapWidth = 50 * 16;
        float mapHeight = 50 * 16;

        // Tính kích thước viewport (phần nhìn thấy)
        float viewportWidth = Gdx.graphics.getWidth() * camera.zoom;
        float viewportHeight = Gdx.graphics.getHeight() * camera.zoom;

        // Clamp X - Chỉ clamp khi viewport <= map
        float minX = viewportWidth / 2;
        float maxX = mapWidth - viewportWidth / 2;
        
        if (minX <= maxX) {
            // Viewport nhỏ hơn hoặc bằng map: clamp bình thường
            camera.position.x = Math.max(minX, Math.min(camera.position.x, maxX));
        }
        // Nếu viewport > map: không clamp, camera di chuyển tự do

        // Clamp Y - Chỉ clamp khi viewport <= map
        float minY = viewportHeight / 2;
        float maxY = mapHeight - viewportHeight / 2;
        
        if (minY <= maxY) {
            // Viewport nhỏ hơn hoặc bằng map: clamp bình thường
            camera.position.y = Math.max(minY, Math.min(camera.position.y, maxY));
        }
        // Nếu viewport > map: không clamp, camera di chuyển tự do
    }

    @Override
    public void dispose() {
        mapManager.dispose();
        shapeRenderer.dispose();
        if (batch != null) {
            batch.dispose();
        }
        if (hud != null) {
            hud.dispose();
        }
        com.ecosystem.sim.util.SoundManager.dispose();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
        if (hud != null && hud.getStage() != null) {
            hud.getStage().getViewport().update(width, height, true);
        }
    }


    // ===== InputProcessor Implementation =====

    @Override
    public boolean keyDown(int keycode) {
        if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
            if (keycode == Input.Keys.PLUS || keycode == Input.Keys.EQUALS) {
                mapManager.adjustZoom(-0.1f); // Phóng to (camera zoom nhỏ hơn = phóng to)
                return true;
            } else if (keycode == Input.Keys.MINUS) {
                mapManager.adjustZoom(0.1f); // Thu nhỏ (camera zoom lớn hơn = thu nhỏ)
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Chỉ cho phép drag với chuột trái (button 0)
        if (button == 0) {
            isDragging = true;
            lastMouseX = screenX;
            lastMouseY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button == 0) {
            isDragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        isDragging = false;
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (isDragging) {
            // Lấy vị trí chuột hiện tại trong tọa độ thế giới (World)
            Vector3 lastPos = camera.unproject(new Vector3(lastMouseX, lastMouseY, 0));
            Vector3 currPos = camera.unproject(new Vector3(screenX, screenY, 0));

            // Tính khoảng cách dịch chuyển thực tế
            float deltaX = currPos.x - lastPos.x;
            float deltaY = currPos.y - lastPos.y;

            // Di chuyển camera ngược hướng với hướng kéo chuột để tạo cảm giác "cầm map kéo đi"
            camera.position.x -= deltaX;
            camera.position.y -= deltaY;

            lastMouseX = screenX;
            lastMouseY = screenY;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        // Mouse scroll up = positive (phóng to), scroll down = negative (thu nhỏ)
        if (amountY > 0) {
            mapManager.adjustZoom(-0.1f); // Cuộn lên = phóng to (zoom nhỏ hơn)
        } else if (amountY < 0) {
            mapManager.adjustZoom(0.1f); // Cuộn xuống = thu nhỏ (zoom lớn hơn)
        }
        return true;
    }
}
