package com.ecosystem.sim.entity.concrete;
import com.badlogic.gdx.graphics.Color;
import com.ecosystem.sim.entity.Herbivore;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.EntityManager;

public class Elephant extends Herbivore {//Lớp Voi - Động vật ăn cỏ, màu sắc: Xám
    public Elephant(float x, float y, MapManager mapManager) {
        super(x, y, Color.GRAY, mapManager, 12, 12);
        // Thuộc tính 
        this.speed = 50f;           
        this.bodySize = 4;           
        this.dominance = 100;        // Uy quyền cao nhất
        this.senseRadius = 150f;     
        this.ignoreWater = true;     // Lội được nước
        this.ediblePlantType = Tree.class; // Chỉ ăn tree
        this.health = 150;
        this.maxHealth = 150;
        this.energy = 100;
        this.maxEnergy = 100;
        this.hydration = 100;
        this.maxHydration = 100;
        this.hungerRate = 2.5f;
        this.thirstRate = 4.0f;     // Động vật ăn cỏ khát nhanh hơn ăn thịt
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.speed = 50f;
    }

    @Override
    public void cloneSelf() {
        EntityManager em = EntityManager.getInstance();
        if (em != null) {
            em.spawnElephant(position.x, position.y);
        }
    }


}
