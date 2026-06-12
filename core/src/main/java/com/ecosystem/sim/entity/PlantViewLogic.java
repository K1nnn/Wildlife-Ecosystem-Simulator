package com.ecosystem.sim.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Lớp chịu trách nhiệm về hiển thị đồ họa (Rendering/View Logic) cho Thực vật
 * Tách biệt hoàn toàn việc hiển thị lên màn hình khỏi logic sinh học
 */
public class PlantViewLogic {

    /**
     * Thực hiện vẽ hình vuông đại diện cho thực vật lên màn hình
     */
    public static void draw(ShapeRenderer shapeRenderer, Vector2 position, float width, float height, 
                            Color color, Plant.GrowthStage growthStage, boolean isAlive) {
        if (isAlive && growthStage != null) {
            shapeRenderer.setColor(color);
            float sizeMultiplier = growthStage.sizeMultiplier;
            float actualWidth = width * sizeMultiplier;
            float actualHeight = height * sizeMultiplier;
            
            // Căn giữa hình chữ nhật
            float centerX = position.x + (width - actualWidth) / 2;
            float centerY = position.y + (height - actualHeight) / 2;
            
            // Vẽ hình vuông
            shapeRenderer.rect(centerX, centerY, actualWidth, actualHeight);
        }
    }
}
