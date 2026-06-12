package com.ecosystem.sim.entity.concrete;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.ecosystem.sim.entity.Animal;
import com.ecosystem.sim.entity.Herbivore;
import com.ecosystem.sim.entity.AnimalState;
import com.ecosystem.sim.entity.behavior.IPrey;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.EntityManager;

public class Rabbit extends Herbivore implements IPrey {//Lớp Thỏ - Động vật ăn cỏ, màu sắc: Xanh lá nhạt
    public Rabbit(float x, float y, MapManager mapManager) {
        super(x, y, new Color(0, 1, 0, 1), mapManager, 12, 12); // 12x12 pixels, GREEN
        // Thuộc tính 
        this.speed = 80f;           
        this.fleeSpeed = 150f;      // Tốc độ chạy thoát
        this.bodySize = 1;          // Lách được qua bụi rậm
        this.dominance = 20;        
        this.senseRadius = 150f;    
        this.health = 80;
        this.maxHealth = 80;
        this.energy = 100;
        this.maxEnergy = 100;
        this.hydration = 100;
        this.maxHydration = 100;
        this.hungerRate = 2f;// Thỏ ăn cỏ nên tiêu tốn ít năng lượng hơn sói
        this.thirstRate = 4f;
        this.fleeDistance = 200f;
        this.threatDetected = null;
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.threatDetected = null;
        this.speed = 80f; // Tốc độ bình thường
    }

    @Override
    public void reset() {
        super.reset();
        this.threatDetected = null;
    }

    @Override
    protected AnimalState makeDecision() {
        if (threatDetected != null && threatDetected.isAlive() && //priority cao nhất là chạy khi phát hiện threat
            position.dst(threatDetected.getPosition()) < fleeDistance) {
            return AnimalState.FLEEING;
        }
        return super.makeDecision();// Quy trình quyết định bình thường (bao gồm kiểm tra đói ăn trong Herbivore)
    }


    @Override
    public void cloneSelf() {
        EntityManager em = EntityManager.getInstance();
        if (em != null) {
            em.spawnRabbit(position.x, position.y);
        }
    }
}
