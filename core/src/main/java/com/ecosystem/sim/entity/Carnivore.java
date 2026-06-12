package com.ecosystem.sim.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.ecosystem.sim.entity.behavior.IPredator;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.ResourceTracker;

import java.util.List;

/**
 * Lớp trừu tượng trung gian cho các động vật ăn thịt (Sói, Hổ...)
 * Chứa toàn bộ thuộc tính vật lý, sinh lý và logic săn bắn chung
 */
public abstract class Carnivore extends Animal implements IPredator {
    protected float huntSpeed;
    protected float huntingRange;
    protected Animal preyDetected;

    public Carnivore(float x, float y, Color color, MapManager mapManager,
                     float width, float height) {
        super(x, y, color, mapManager, width, height);
        this.preyDetected = null;
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.preyDetected = null;
    }

    @Override
    public void reset() {
        super.reset();
        this.preyDetected = null;
    }



    @Override
    protected AnimalState makeDecision() {
        // 1. Đang uống nước thì uống nốt
        if (currentState == AnimalState.DRINKING) {
            if (stateTimer < 2.0f) return AnimalState.DRINKING;
        }

        // 2. Đang ăn thịt, đứng ăn nốt 2 giây
        if (currentState == AnimalState.EATING) {
            if (stateTimer < 2.0f) return AnimalState.EATING;
            
            // Năng lượng trên 60 mới được sinh sản (khắt khe vừa phải, tính đến sụt năng lượng khi ăn)
            if (energy >= 40) {
                energy -= 10; // Tiêu hao năng lượng cho việc sinh sản
                return AnimalState.REPRODUCING;
            }
        }

        // Đang sinh sản thì đứng im 1.5 giây
        if (currentState == AnimalState.REPRODUCING) {
            if (stateTimer < 1.5f) return AnimalState.REPRODUCING;

            // HOÀN THÀNH SINH SẢN: Gọi hàm nhân bản
            cloneSelf();
            return AnimalState.WANDERING; // Đẻ xong thì đi dạo
        }

        // 3. Ưu tiên đi tìm nước nếu hydration < 50
        if (hydration < 50f) {
            return AnimalState.SEARCHING_WATER;
        }

        // 4. Ưu tiên: Nếu phát hiện con mồi và mồi vẫn sống trong tầm săn, truy đuổi
        if (preyDetected != null && preyDetected.isAlive() &&
            position.dst(preyDetected.getPosition()) < huntingRange) {
            return AnimalState.HUNTING;
        }

        // 5. Nếu đói (energy < 60) nhưng chưa có target, quét tìm con mồi qua ResourceTracker và săn nếu tìm thấy
        if (energy < 60f) {
            Animal closestPrey = detectPrey(null);
            if (closestPrey != null) {
                hunt(closestPrey);
                return AnimalState.HUNTING;
            }
        }

        return super.makeDecision();
    }

    @Override
    protected void move(float deltaTime) {
        if (currentState == AnimalState.HUNTING && preyDetected != null) {
            float tempSpeed = this.speed;
            this.speed = huntSpeed;

            // Cập nhật vị trí mục tiêu theo thời gian thực
            setTargetPosition(preyDetected.getPosition());

            super.move(deltaTime);
            this.speed = tempSpeed;

            // Kiểm tra xem đã tiếp cận để bắt được con mồi chưa
            float captureDistance = (width + preyDetected.getWidth()) / 2f + 4f;
            if (position.dst(preyDetected.getPosition()) < captureDistance) {
                onPreyCaptured(preyDetected);
                preyDetected = null;
            }
        } else {
            super.move(deltaTime);
        }
    }

    /**
     * Mẫu thiết kế Template Method - Hook kích hoạt khi bắt được con mồi
     */
    protected void onPreyCaptured(Animal prey) {
        consumeAnimal(prey);
    }

    @Override
    public void hunt(Animal prey) {
        this.preyDetected = prey;
        this.targetAnimal = prey;
        this.currentState = AnimalState.HUNTING;
        setTargetPosition(prey.getPosition());
    }

    @Override
    public Animal detectPrey(List<Entity> potentialPrey) {
        // Tối ưu hóa Zero-Allocation thông qua ResourceTracker
        return ResourceTracker.getInstance().findNearestPrey(position, huntingRange, this.dominance);
    }

    @Override
    public float getHuntingRange() {
        return huntingRange;
    }


    @Override
    protected void onStateChanged(AnimalState oldState, AnimalState newState) {
        super.onStateChanged(oldState, newState);
        if (oldState == AnimalState.HUNTING && newState != AnimalState.HUNTING) {
            // Dừng săn bắn, xóa sạch mục tiêu
            preyDetected = null;
            targetAnimal = null;
        }
    }
}
