package com.ecosystem.sim.util;

import com.badlogic.gdx.math.Vector2;
import com.ecosystem.sim.entity.Animal;
import com.ecosystem.sim.entity.Plant;
import com.ecosystem.sim.map.MapManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Quản lý & Theo dõi Tài nguyên Môi trường (Nước, Thực phẩm) theo mẫu Singleton
 * Giúp đồng bộ hóa cơ chế săn mồi, ăn thực thể và uống nước hiệu năng cao
 */
public class ResourceTracker {
    private static final ResourceTracker instance = new ResourceTracker();
    public static ResourceTracker getInstance() {
        return instance;
    }

    private final List<Vector2> waterSources = new ArrayList<>();
    private final List<Plant> registeredPlants = new ArrayList<>();
    private final List<Animal> registeredHerbivores = new ArrayList<>();
    private boolean initialized = false;

    private ResourceTracker() {}

    /**
     * Khởi tạo và quét toàn bộ bản đồ để lưu danh sách các ô nước
     */
    public void initialize(MapManager mapManager) {
        waterSources.clear();
        registeredPlants.clear();
        registeredHerbivores.clear();
        
        // Quét toàn bộ lưới gạch 50x50 để tìm ô nước và lưu lại tọa độ trung tâm
        for (int tx = 0; tx < 50; tx++) {
            for (int ty = 0; ty < 50; ty++) {
                float wx = tx * 16f + 8f;
                float wy = ty * 16f + 8f;
                if (mapManager.isWater(wx, wy)) {
                    waterSources.add(new Vector2(wx, wy));
                }
            }
        }
        initialized = true;
    }

    /**
     * Đăng ký một thực thể thực vật mới
     */
    public void registerPlant(Plant plant) {
        if (plant != null && !registeredPlants.contains(plant)) {
            registeredPlants.add(plant);
        }
    }

    /**
     * Hủy đăng ký một thực thể thực vật (đã bị ăn hết hoặc héo chết)
     */
    public void unregisterPlant(Plant plant) {
        if (plant != null) {
            registeredPlants.remove(plant);
        }
    }

    /**
     * Đăng ký một động vật ăn cỏ còn sống làm con mồi cho thú săn
     */
    public void registerHerbivore(Animal animal) {
        if (animal != null && !registeredHerbivores.contains(animal)) {
            registeredHerbivores.add(animal);
        }
    }

    /**
     * Hủy đăng ký một động vật ăn cỏ (đã bị ăn thịt hoặc già chết)
     */
    public void unregisterHerbivore(Animal animal) {
        if (animal != null) {
            registeredHerbivores.remove(animal);
        }
    }

    /**
     * Tìm nguồn nước (ô gạch nước) gần vị trí thực thể nhất
     */
    public Vector2 findNearestWater(Vector2 position) {
        Vector2 nearest = null;
        float minDistance = Float.MAX_VALUE;
        
        for (Vector2 water : waterSources) {
            float distance = position.dst(water);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = water;
            }
        }
        return nearest;
    }

    /**
     * Tìm thực vật trưởng thành gần nhất làm thức ăn cho động vật ăn cỏ
     */
    public Plant findNearestPlantFood(Vector2 position, float maxRange, Class<? extends Plant> plantClass) {
        Plant nearest = null;
        float minDistance = maxRange;
        
        for (int i = 0; i < registeredPlants.size(); i++) {
            Plant plant = registeredPlants.get(i);
            if (plant != null && plant.isAlive() && plant.getGrowthStage() == Plant.GrowthStage.MATURE && plantClass.isInstance(plant)) {
                float distance = position.dst(plant.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = plant;
                }
            }
        }
        return nearest;
    }

    /**
     * Tìm con mồi động vật ăn cỏ gần nhất và có dominance thấp hơn để săn mồi
     */
    public Animal findNearestPrey(Vector2 position, float maxRange, int dominanceThreshold) {
        Animal nearest = null;
        float minDistance = maxRange;
        
        for (int i = 0; i < registeredHerbivores.size(); i++) {
            Animal herbivore = registeredHerbivores.get(i);
            if (herbivore != null && herbivore.isAlive() && herbivore.getDominance() < dominanceThreshold) {
                float distance = position.dst(herbivore.getPosition());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = herbivore;
                }
            }
        }
        return nearest;
    }

    public void clear() {
        waterSources.clear();
        registeredPlants.clear();
        registeredHerbivores.clear();
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
