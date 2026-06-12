package com.ecosystem.sim.entity;

import com.badlogic.gdx.math.MathUtils;

/**
 * Lớp chịu trách nhiệm tính toán sinh học (survival stats, đói, khát, lão hóa)
 * Tách biệt hoàn toàn phần xử lý logic sinh tồn khỏi phần vẽ (View) và thực thể (Entity)
 */
public class AnimalBioLogic {
    
    /**
     * Tính toán chỉ số năng lượng (energy) giảm dần theo thời gian đói
     */
    public static float calculateEnergy(float energy, float hungerRate, float deltaTime) {
        float nextEnergy = energy - hungerRate * deltaTime;
        return Math.max(0, nextEnergy);
    }
    
    /**
     * Tính toán chỉ số độ ẩm (hydration) giảm dần theo thời gian khát
     */
    public static float calculateHydration(float hydration, float thirstRate, float deltaTime) {
        float nextHydration = hydration - thirstRate * deltaTime;
        return Math.max(0, nextHydration);
    }
    
    /**
     * Tính toán chỉ số máu (health) giảm dần khi thiếu đói/khát nghiêm trọng
     * Hoặc hồi phục đầy 100% máu lập tức khi động vật đang ăn
     */
    public static float calculateHealth(float health, float maxHealth, float energy, float hydration, 
                                        float healthDecayRate, float deltaTime, AnimalState currentState) {
        float nextHealth = health;
        
        // Sức khỏe giảm nếu đói hoặc khát nghiêm trọng (dưới 20)
        if (energy < 20) nextHealth -= healthDecayRate * deltaTime;
        if (hydration < 20) nextHealth -= healthDecayRate * deltaTime;
        
        // Hồi phục sức khỏe (hồi máu) lập tức khi ăn
        if (currentState == AnimalState.EATING) {
            nextHealth = maxHealth;
        }
        
        return MathUtils.clamp(nextHealth, 0, maxHealth);
    }
}
