package com.ecosystem.sim.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.ecosystem.sim.entity.concrete.Grass;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.ResourceTracker;

/**
 * Lớp trừu tượng trung gian cho các động vật ăn cỏ (Thỏ, Hươu...)
 * Quản lý cơ chế tìm kiếm thức ăn từ thực thể cỏ hoặc ô gạch cỏ
 */
public abstract class Herbivore extends Animal {
    protected Plant foodDetected;
    public Class<? extends Plant> ediblePlantType = Grass.class; // Phân hóa thực phẩm mặc định là cỏ
    
    // Thuộc tính chạy trốn (áp dụng cho con mồi)
    protected float fleeSpeed;
    protected float fleeDistance;
    protected Animal threatDetected;

    public Herbivore(float x, float y, Color color, MapManager mapManager,
                     float width, float height) {
        super(x, y, color, mapManager, width, height);
        this.foodDetected = null;
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.foodDetected = null;
        this.threatDetected = null;
    }

    @Override
    public void reset() {
        super.reset();
        this.foodDetected = null;
        this.threatDetected = null;
    }

    @Override
    protected void onStateChanged(AnimalState oldState, AnimalState newState) {
        super.onStateChanged(oldState, newState);
        if (oldState == AnimalState.SEARCHING_FOOD && newState != AnimalState.SEARCHING_FOOD) {
            foodDetected = null;
        }
        if (oldState == AnimalState.FLEEING && newState != AnimalState.FLEEING) {
            threatDetected = null;
            targetAnimal = null;
        }
    }

    @Override
    protected AnimalState makeDecision() {
        if (threatDetected != null && threatDetected.isAlive() &&
            position.dst(threatDetected.getPosition()) < fleeDistance) {
            return AnimalState.FLEEING;
        }

        // Đang uống nước thì uống nốt
        if (currentState == AnimalState.DRINKING) {
            if (stateTimer < 2.0f) return AnimalState.DRINKING;
        }

        // Nếu đang ăn, ăn xong chuyển sang sinh sản
        if (currentState == AnimalState.EATING) {
            if (stateTimer < 2.0f) return AnimalState.EATING; 

            // Năng lượng trên 30 mới được đẻ (khắt khe vừa phải, tính đến sụt năng lượng khi ăn)
            if (energy >= 30) {
                energy -= 10; // Tiêu hao năng lượng cho việc đẻ
                return AnimalState.REPRODUCING;
            }
        }

        // Đang đẻ thì đứng im 1-2 giây
        if (currentState == AnimalState.REPRODUCING) {
            if (stateTimer < 1.5f) return AnimalState.REPRODUCING;

            // HOÀN THÀNH ĐẺ: Gọi hàm sinh sản
            cloneSelf();
            cloneSelf();
            return AnimalState.WANDERING; // Đẻ xong thì đi dạo
        }

        // Sử dụng logic sinh lý chuẩn của lớp cha (ưu tiên nước hydration < 50 trước đói ăn energy < 60)
        return super.makeDecision();
    }

    /**
     * Tìm kiếm thực vật làm thức ăn qua ResourceTracker
     */
    public Plant detectFood() {
        return ResourceTracker.getInstance().findNearestPlantFood(position, senseRadius, ediblePlantType);
    }

    public void setTargetFood(Plant food) {
        this.foodDetected = food;
        if (food != null) {
            setTargetPosition(food.getPosition());
        }
    }

    @Override
    public void specificBehavior(float deltaTime) {
        if (currentState == AnimalState.SEARCHING_FOOD) {
            // 1. Ưu tiên di chuyển tới thực thể cỏ/cây đã phát hiện
            if (foodDetected != null && foodDetected.isAlive() && ediblePlantType.isInstance(foodDetected)) {
                setTargetPosition(foodDetected.getPosition());
                
                // Nếu đến rất gần thực thể cỏ/cây, tiến hành ăn (va chạm ăn thực sự)
                float eatDistance = (width + foodDetected.getWidth()) / 2f + 4f;
                if (position.dst(foodDetected.getPosition()) < eatDistance) {
                    float foodValue = foodDetected.getNutritionalValue();
                    if (foodValue > 0) {
                        eat(foodValue);
                        foodDetected.beEaten();
                        foodDetected = null;
                    }
                }
            } else {
                // Không tìm thấy thực thể thực vật thức ăn nào gần đó, chuyển sang lang thang tiếp
                currentState = AnimalState.WANDERING;
            }
        } else if (currentState == AnimalState.FLEEING && threatDetected != null && threatDetected.isAlive()) {
            Vector2 fleeDirection = new Vector2(position).sub(threatDetected.getPosition()).nor();
            targetPosition.set(position).add(fleeDirection.scl(fleeDistance));
        }
    }

    @Override
    protected void move(float deltaTime) {
        if (currentState == AnimalState.FLEEING) {
            float tempSpeed = this.speed;
            this.speed = fleeSpeed;
            super.move(deltaTime);
            this.speed = tempSpeed;
        } else {
            super.move(deltaTime);
        }
    }

    public void flee(Animal predator) {
        this.threatDetected = predator;
        this.targetAnimal = predator;
        this.currentState = AnimalState.FLEEING;
        
        Vector2 fleeDirection = new Vector2(position).sub(predator.getPosition()).nor();
        targetPosition.set(position).add(fleeDirection.scl(fleeDistance));
    }

    public Animal detectThreat(java.util.List<Animal> potentialThreats) {
        Animal closestThreat = null;
        float minDistance = senseRadius;
        
        for (Animal threat : potentialThreats) {
            if (!threat.isAlive()) continue;
            float distance = position.dst(threat.getPosition());
            if (distance < minDistance && threat.getDominance() > this.dominance) {
                closestThreat = threat;
                minDistance = distance;
            }
        }
        return closestThreat;
    }

    public float getAwarenessRange() {
        return senseRadius;
    }
}
