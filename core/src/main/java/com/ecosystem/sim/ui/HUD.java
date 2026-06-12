package com.ecosystem.sim.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.ecosystem.sim.util.EntityManager;
import com.ecosystem.sim.util.Season;

/**
 * Hệ thống HUD (Heads-Up Display) hiển thị thông tin mô phỏng và bảng điều khiển mùa dùng Scene2D
 */
public class HUD {
    private BitmapFont font;
    private EntityManager entityManager;
    
    // Scene2D UI
    private Stage stage;
    private Skin skin;
    private TextButton btnSeason;
    private TextButton btnAuto;
    

    public HUD(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.font = new BitmapFont();
        this.font.setColor(Color.WHITE);
        
        // Khởi tạo Scene2D Stage với ScreenViewport độc lập
        this.stage = new Stage(new ScreenViewport());
        this.skin = createProgrammaticSkin();
        setupUI();
    }

    /**
     * Tạo một Skin bằng lập trình trực tiếp (Programmatic Skin) không phụ thuộc file assets ngoài
     */
    private Skin createProgrammaticSkin() {
        Skin skin = new Skin();
        
        // Tạo Texture màu trắng kích thước 1x1 pixel
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture whiteTexture = new Texture(pixmap);
        pixmap.dispose();
        
        skin.add("white", whiteTexture);
        skin.add("default", font);
        
        // Định nghĩa Style cho TextButton
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        
        // Màu nền mặc định khi không click: màu xám đậm mờ
        textButtonStyle.up = skin.newDrawable("white", new Color(0.15f, 0.15f, 0.2f, 0.7f));
        textButtonStyle.down = skin.newDrawable("white", new Color(0.35f, 0.35f, 0.4f, 0.9f));
        textButtonStyle.over = skin.newDrawable("white", new Color(0.25f, 0.25f, 0.3f, 0.8f));
        
        skin.add("default", textButtonStyle);
        return skin;
    }

    /**
     * Cài đặt các thành phần UI Scene2D
     */
    private void setupUI() {
        Table table = new Table();
        table.top().right();
        table.setFillParent(true);
        
        // Thiết lập kích thước mặc định và padding cho các nút bấm
        table.defaults().pad(5).width(180).height(38);
        
        btnSeason = new TextButton("Season: BREEDING", skin);
        btnAuto = new TextButton("Auto Cycle: ON", skin);
        
        // Bắt sự kiện click nút Mùa
        btnSeason.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Season current = EntityManager.getCurrentSeason();
                EntityManager.setCurrentSeason(current == Season.BREEDING ? Season.DROUGHT : Season.BREEDING);
                updateButtonStyles();
            }
        });
        
        // Bắt sự kiện click nút Tự Động
        btnAuto.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean auto = EntityManager.isAutoCycleEnabled();
                EntityManager.setAutoCycleEnabled(!auto);
                updateButtonStyles();
            }
        });
        
        table.add(btnSeason).padTop(15).padRight(15).row();
        table.add(btnAuto).padRight(15).row();
        
        stage.addActor(table);
        updateButtonStyles();
    }

    /**
     * Cập nhật màu sắc và văn bản hiển thị trên nút bấm tương ứng trạng thái thực tế
     */
    public void updateButtonStyles() {
        Season current = EntityManager.getCurrentSeason();
        boolean auto = EntityManager.isAutoCycleEnabled();
        
        btnSeason.setText("Season: " + (current == Season.BREEDING ? "BREEDING" : "DROUGHT"));
        btnAuto.setText("Auto Cycle: " + (auto ? "ON" : "OFF"));
        
        // Đổi màu sắc (tinting) nút bấm
        if (current == Season.BREEDING) {
            btnSeason.setColor(new Color(0.2f, 0.8f, 0.3f, 1.0f)); // Xanh lá
        } else {
            btnSeason.setColor(new Color(0.9f, 0.5f, 0.1f, 1.0f)); // Cam
        }
        
        if (auto) {
            btnAuto.setColor(new Color(0.2f, 0.5f, 0.9f, 1.0f)); // Xanh dương
        } else {
            btnAuto.setColor(new Color(0.5f, 0.5f, 0.5f, 1.0f)); // Xám
        }
    }

    /**
     * Vẽ HUD lên màn hình
     */
    public void render(SpriteBatch batch) {
        int animalCount = entityManager.getAnimalCount();
        int plantCount = entityManager.getPlantCount();
        int fps = Gdx.graphics.getFramesPerSecond();
        
        Season currentSeason = EntityManager.getCurrentSeason();
        boolean autoEnabled = EntityManager.isAutoCycleEnabled();
        
        // Đồng bộ nhãn của nút
        updateButtonStyles();
        
        // Thiết lập ma trận chiếu của batch theo viewport của stage để giữ vị trí chính xác khi resize cửa sổ
        batch.setProjectionMatrix(stage.getCamera().combined);
        
        batch.begin();
        
        // Vẽ thông tin ở góc trên bên trái
        float x = 15;
        float y = Gdx.graphics.getHeight() - 15;
        
        font.draw(batch, "Animals: " + animalCount + " (Rabbits: " + entityManager.getRabbitCount() + ", Deers: " + entityManager.getDeerCount() + ", Elephants: " + entityManager.getElephantCount() + ", Wolves: " + entityManager.getWolfCount() + ", Tigers: " + entityManager.getTigerCount() + ")", x, y);
        y -= 22;
        
        font.draw(batch, "Plants: " + plantCount + " (Grass: " + entityManager.getGrassCount() + ", Trees: " + entityManager.getTreeCount() + ")", x, y);
        y -= 22;
        
        font.draw(batch, "FPS: " + fps, x, y);
        y -= 22;
        
        // Ecosystem Status
        String ecoStatus = getEcosystemStatus(animalCount, plantCount);
        font.draw(batch, "Status: " + ecoStatus, x, y);
        y -= 22;
        
        // Season Info
        String seasonText = "Current Season: " + (currentSeason == Season.BREEDING ? "BREEDING" : "DROUGHT");
        if (autoEnabled) {
            seasonText += String.format(" (Auto change in: %.1fs)", EntityManager.getSeasonTimeLeft());
        }
        font.draw(batch, seasonText, x, y);
        
        batch.end();
        
        // Vẽ Scene2D stage (các nút bấm góc bên phải)
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    /**
     * Xác định trạng thái của hệ sinh thái
     */
    private String getEcosystemStatus(int animals, int plants) {
        if (animals == 0) {
            return "Extinct";
        }
        
        float ratio = animals > 0 ? (float) plants / animals : 0;
        
        if (ratio > 5) {
            return "Balanced";
        } else if (ratio > 2) {
            return "Stable";
        } else if (ratio > 1) {
            return "Stressed";
        } else {
            return "Critical";
        }
    }

    public Stage getStage() {
        return stage;
    }

    /**
     * Dọn dẹp tài nguyên
     */
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
        if (skin != null) {
            skin.dispose();
        }
    }
}
