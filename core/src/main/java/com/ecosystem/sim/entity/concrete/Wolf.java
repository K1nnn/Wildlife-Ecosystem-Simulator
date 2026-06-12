package com.ecosystem.sim.entity.concrete;

import com.badlogic.gdx.graphics.Color;
import com.ecosystem.sim.entity.Carnivore;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.EntityManager;

/**
 * Lớp Sói - Động vật ăn thịt, kế thừa Carnivore
 * Màu sắc: Đỏ
 */
public class Wolf extends Carnivore {
    private float howlTimer = 0f;
    
    public Wolf(float x, float y, MapManager mapManager) {
        // Gọi constructor Carnivore với màu đỏ và kích thước 14x14
        super(x, y, new Color(1, 0, 0, 1), mapManager, 12, 12);
        
        // Thuộc tính vật lý sói
        this.speed = 90f;           // Tốc độ bình thường
        this.huntSpeed = 140f;      // Tốc độ truy đuổi
        this.bodySize = 2;          // Sói to - bị chặn bởi bụi rậm nhỏ
        this.dominance = 60;        // Trung bình - có quyền lực
        this.senseRadius = 180f;    // Tầm nhìn tốt
        this.huntingRange = 200f;
        
        // Sinh lý sói
        this.health = 100;
        this.maxHealth = 100;
        this.energy = 100;
        this.maxEnergy = 100;
        this.hydration = 100;
        this.maxHydration = 100;
        
        // Sinh lý ăn thịt
        this.hungerRate = 3f;
        this.thirstRate = 1.5f;
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.speed = 90f; // Reset về tốc độ bình thường
    }

    @Override
    public void cloneSelf() {
        EntityManager em = EntityManager.getInstance();
        if (em != null) {
            em.spawnWolf(position.x, position.y);
        }
    }

    @Override
    public void specificBehavior(float deltaTime) {
        if (currentState == com.ecosystem.sim.entity.AnimalState.EATING) {
            howlTimer -= deltaTime;
            if (howlTimer <= 0) {
                com.ecosystem.sim.util.SoundManager.playWolfHunt();
                howlTimer = 5.0f; // Chờ 5s trước khi phát âm thanh tiếp
            }
        } else {
            howlTimer = 0f; // Reset khi không ăn
        }
    }
}
