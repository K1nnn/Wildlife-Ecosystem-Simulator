package com.ecosystem.sim.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Lớp chịu trách nhiệm về hiển thị đồ họa (Rendering/View Logic) cho Động vật
 * Tách biệt hoàn toàn việc hiển thị lên màn hình khỏi logic sinh học
 */
public class AnimalViewLogic {
    
    /**
     * Thực hiện vẽ hình tròn đại diện cho động vật lên màn hình
     */
    public static void draw(ShapeRenderer shapeRenderer, Vector2 position, float width, float height, Color color, boolean isAlive) {
        if (isAlive) {
            shapeRenderer.setColor(color);
            // Vẽ hình tròn tại vị trí trung tâm của thực thể
            shapeRenderer.circle(position.x + width / 2, position.y + height / 2, width / 2);
        }
    }
}
