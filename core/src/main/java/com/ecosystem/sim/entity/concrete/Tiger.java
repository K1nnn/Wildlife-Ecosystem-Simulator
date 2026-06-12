package com.ecosystem.sim.entity.concrete;
import com.badlogic.gdx.graphics.Color;
import com.ecosystem.sim.entity.Animal;
import com.ecosystem.sim.entity.AnimalState;
import com.ecosystem.sim.entity.Carnivore;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.EntityManager;

public class Tiger extends Carnivore {//Lớp Hổ - Động vật ăn thịt mạnh nhất, kế thừa Carnivore , màu sắc: Cam 
    private int killCount; // Số lần săn thành công đặc thù của Hổ
    private float roarTimer = 0f;
    
    public Tiger(float x, float y, MapManager mapManager) {
        super(x, y, new Color(1, 0.6f, 0, 1), mapManager, 12, 12);
        // Thuộc tính 
        this.speed = 85f;
        this.huntSpeed = 120f;
        this.bodySize = 3;           // Hổ rất to - bị chặn bởi bụi rậm
        this.dominance = 80;        
        this.senseRadius = 200f;     // Tầm nhìn xa nhất
        this.huntingRange = 220f;
        this.health = 120;
        this.maxHealth = 120;
        this.energy = 100;
        this.maxEnergy = 100;
        this.hydration = 100;
        this.maxHydration = 100;
        this.hungerRate = 4f;//Hổ đói
        this.thirstRate = 2.0f;
        this.killCount = 0;
    }

    @Override
    protected void updateAIState(float deltaTime) {
        super.updateAIState(deltaTime);
        // Hổ có thể vào trạng thái RESTING sau khi ăn
        if (currentState == AnimalState.EATING && stateTimer > 3.0f) {
            currentState = AnimalState.RESTING;
        }
    }

    @Override
    protected AnimalState makeDecision() {
        // Quy trình quyết định đặc thù của Hổ: phục hồi sau khi ăn hoặc khi yếu máu
        if (health < maxHealth * 0.7f) {
            return AnimalState.RESTING;
        }
        // Nếu không yếu máu, áp dụng logic quyết định săn mồi chung ở lớp cha Carnivore
        return super.makeDecision();
    }

    @Override
    protected void onPreyCaptured(Animal prey) {
        super.onPreyCaptured(prey);// Thực thi ăn thịt từ lớp cha Carnivore
        killCount++;// Tăng đếm mạng săn đặc thù
    }

    @Override
    public void cloneSelf() {
        EntityManager em = EntityManager.getInstance();
        if (em != null) {
            em.spawnTiger(position.x, position.y);
        }
    }

    @Override
    public void specificBehavior(float deltaTime) {
        if (currentState == AnimalState.EATING) {
            roarTimer -= deltaTime;
            if (roarTimer <= 0) {
                com.ecosystem.sim.util.SoundManager.playTigerHunt();
                roarTimer = 3.0f; // Chờ 3s trước khi phát âm thanh tiếp
            }
        } else {
            roarTimer = 0f; // Reset khi không ăn
        }
    }

    public int getKillCount() {
        return killCount;
    }
}
