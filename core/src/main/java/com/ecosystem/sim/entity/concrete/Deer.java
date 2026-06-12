package com.ecosystem.sim.entity.concrete;

import com.badlogic.gdx.graphics.Color;
import com.ecosystem.sim.entity.Animal;
import com.ecosystem.sim.entity.Herbivore;
import com.ecosystem.sim.entity.AnimalState;
import com.ecosystem.sim.entity.behavior.IPrey;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.EntityManager;

public class Deer extends Herbivore implements IPrey {//Lớp Hươu - Động vật ăn cỏ, màu sắc: Vàng nhạt 
    public Deer(float x, float y, MapManager mapManager) {
        super(x, y, new Color(1, 1, 0.7f, 1), mapManager, 12, 12); 
        // Thuộc tính 
        this.speed = 75f;
        this.fleeSpeed = 130f;
        this.bodySize = 1;        // Hươu cũng nhỏ, có thể lách bụi rậm
        this.dominance = 40;      // Uy quyền 40
        this.senseRadius = 160f;
        this.health = 90;
        this.maxHealth = 90;
        this.energy = 100;
        this.maxEnergy = 100;
        this.hydration = 100;
        this.maxHydration = 100;
        this.hungerRate = 3f;
        this.thirstRate = 5f;
        this.fleeDistance = 200f;
        this.threatDetected = null;
    }

    @Override
    protected AnimalState makeDecision() {
        if (threatDetected != null && threatDetected.isAlive() &&
            position.dst(threatDetected.getPosition()) < fleeDistance) {
            return AnimalState.FLEEING;
        }
        return super.makeDecision();
    }


    @Override
    public void cloneSelf() {
        EntityManager em = EntityManager.getInstance();
        if (em != null) {
            em.spawnDeer(position.x, position.y);
        }
    }

}
