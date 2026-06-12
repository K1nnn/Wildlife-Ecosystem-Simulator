package com.ecosystem.sim.entity.concrete;
import com.badlogic.gdx.graphics.Color;
import com.ecosystem.sim.entity.Plant;
import com.ecosystem.sim.map.MapManager;


public class Grass extends Plant {//Lớp Cỏ - Thực vật, màu sắc: Xanh lá nhạt
    
    public Grass(float x, float y, MapManager mapManager) {
        super(x, y, new Color(0.5f, 1, 0.5f, 1), mapManager, 14, 14);
        this.growthRate = 25f; // 25%/s => mất 4 giây từ hạt đến trưởng thành
        this.nutritionalValue = 25f;
        this.maxAge = 150f; //tuổi thọ
        this.wiltheringAge = 100f;
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.growthRate = 25f;
    }

    @Override
    public void specificBehavior(float deltaTime) {//Không có hành vi đặc biệt nào, chỉ phát triển theo thời gian
    }
}
