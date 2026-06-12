package com.ecosystem.sim.entity.concrete;

import com.badlogic.gdx.graphics.Color;
import com.ecosystem.sim.entity.Plant;
import com.ecosystem.sim.map.MapManager;

/**
 * Lớp Cây cao - Thực vật lớn hơn cỏ
 * Phát triển chậm hơn cỏ nhưng cung cấp nhiều dinh dưỡng hơn
 * Màu sắc: Nâu
 */
public class Tree extends Plant {
    
    public Tree(float x, float y, MapManager mapManager) {
        // Gọi constructor cha với màu nâu
        super(x, y, new Color(0.6f, 0.4f, 0.2f, 1), mapManager, 14, 14);
        
        // Cây phát triển chậm
        this.growthRate = 15f; // 15%/s => 6.7 giây từ hạt đến trưởng thành
        this.nutritionalValue = 40f; // Nhiều dinh dưỡng hơn cỏ
        
        // Cây sống lâu nhất
        this.maxAge = 200f; // 3.3 phút
        this.wiltheringAge = 150f;
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.growthRate = 15f;
    }

    @Override
    public void specificBehavior(float deltaTime) {
        // Cây chỉ phát triển và chờ
    }
}
