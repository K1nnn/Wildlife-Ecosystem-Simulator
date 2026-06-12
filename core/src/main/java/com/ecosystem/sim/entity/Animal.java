package com.ecosystem.sim.entity;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Gdx;
import com.ecosystem.sim.map.MapManager;
import com.ecosystem.sim.util.PathFinding;

/**
 * Lớp cơ sở cho tất cả các loài động vật
 * Quản lý các chỉ số sinh tồn, hành vi và tương tác với môi trường
 */
public abstract class Animal extends Entity {
    // === BIẾN LỌC SỰ SỐNG ===
    protected float health;           // Máu (0-100)
    protected float energy;           // Năng lượng (0-100)
    protected float hydration;        // Độ ẩm (0-100)

    
    // === THUỘC TÍNH THỂ CHẤT ===
    protected float speed;            // Tốc độ di chuyển (pixels/s)
    protected int bodySize;           // Kích thước cơ thể: Thỏ=1, Sói=2, Voi=4
    protected int dominance;          // Độ uy quyền: Voi=100, Hổ=80, Sói=60, Thỏ=10
    protected float senseRadius;      // Bán kính tầm nhìn
    
    // === HỆ THỐNG GRAPHIC ===
    protected Color color;            // Màu sắc để vẽ động vật
    
    // === THAM CHIẾU ===
    protected MapManager mapManager;
    
    // === TRẠNG THÁI AI ===
    protected AnimalState currentState;
    protected AnimalState previousState;
    protected float stateTimer;       // Thời gian ở trạng thái hiện tại
    protected Vector2 targetPosition; // Vị trí mục tiêu
    protected Animal targetAnimal;    // Con vật mục tiêu (con mồi hoặc kẻ thù)
    protected boolean ignoreWater = false; // Có thể di chuyển xuyên qua nước hay không
    
    // === THAM SỐ SỐNG ===
    protected float maxHealth;
    protected float maxEnergy;
    protected float maxHydration;
    protected float hungerRate;       // Tốc độ mất energy (per second)
    protected float thirstRate;       // Tốc độ mất hydration (per second)
    protected float healthDecayRate;  // Tốc độ mất health khi thiếu các yếu tố
    
    // === TOÁN THỜI GIAN ===
    protected float stateChangeTimer; // Timer để đổi trạng thái
    protected float decisionTimer;    // Timer để quyết định hành động mới

    // === CHỐNG KẸT (ANTI-STUCK) ===
    protected Vector2 lastPosition = new Vector2();
    protected float stuckTimer;

    // === PATHFINDING (A*) ===
    protected java.util.List<Vector2> currentPath = new java.util.ArrayList<>();
    protected int pathIndex = 0;
    protected float pathRecalcTimer = 0f;
    protected Vector2 pathTargetPos = new Vector2();

    public Animal(float x, float y, Color color, MapManager mapManager,
                  float width, float height) {
        super(x, y, width, height);
        
        this.color = color;
        this.mapManager = mapManager;
        
        // Khởi tạo chỉ số sinh tồn mặc định
        this.health = 100;
        this.maxHealth = 100;
        this.energy = 100;
        this.maxEnergy = 100;
        this.hydration = 100;
        this.maxHydration = 100;
        
        // Khởi tạo các tham số mặc định
        this.hungerRate = 1.5f;    // Tốc độ đói
        this.thirstRate = 2.0f;    // Tốc độ khát
        this.healthDecayRate = 0.5f; // Giảm tốc độ mất health khi đói/khát kiệt quệ xuống 0.5f
        
        // Trạng thái AI - khởi chạy ở WANDERING để animals di chuyển ngay
        this.currentState = AnimalState.WANDERING;
        this.previousState = null;
        this.stateTimer = 0;
        this.targetPosition = new Vector2();
        this.stateChangeTimer = 0;
        this.decisionTimer = 0;
        
        // Khởi tạo biến chống kẹt
        this.lastPosition = new Vector2(x, y);
        this.stuckTimer = 0f;
        
        // Khởi tạo velocity ngẫu nhiên để tránh kẹt ở (0, 0)
        float randomAngle = MathUtils.random(360);
        velocity = new Vector2(1, 0).setAngleDeg(randomAngle);
    }

    @Override
    public void init(float x, float y) {
        super.init(x, y);
        this.health = maxHealth;
        this.energy = maxEnergy;
        this.hydration = maxHydration;
        
        this.currentState = AnimalState.WANDERING;
        this.previousState = null;
        this.stateTimer = 0;
        this.stateChangeTimer = 0;
        this.decisionTimer = 0;
        this.targetAnimal = null;
        this.targetPosition.set(0, 0);
        
        // Reset biến chống kẹt
        this.lastPosition.set(x, y);
        this.stuckTimer = 0f;
        
        // Reset pathfinding
        this.currentPath.clear();
        this.pathIndex = 0;
        this.pathRecalcTimer = 0f;
        this.pathTargetPos.set(0, 0);
        
        float randomAngle = MathUtils.random(360);
        velocity.set(1, 0).setAngleDeg(randomAngle);
    }

    @Override
    public void reset() {
        super.reset();
        this.health = 0;
        this.energy = 0;
        this.hydration = 0;
        this.currentState = AnimalState.IDLE;
        this.targetAnimal = null;
        this.currentPath.clear();
        this.pathIndex = 0;
        this.pathRecalcTimer = 0f;
        this.pathTargetPos.set(0, 0);
    }

    @Override
    public void update(float deltaTime) {
        if (!isAlive) return;
        
        // 1. Cập nhật tuổi tác
        age += deltaTime;
        
        // 2. Giảm các chỉ số sinh tồn theo thời gian
        updateSurvivalStats(deltaTime);
        
        // 3. Kiểm tra tình trạng sống chết
        if (shouldDie()) {
            die();
            return;
        }
        
        // 4. Cập nhật trạng thái AI
        updateAIState(deltaTime);
        
        // 5. Di chuyển dựa trên hành vi hiện tại
        move(deltaTime);
        
        // 6. Gọi logic đặc trưng của từng loài (Đa hình)
        specificBehavior(deltaTime);
    }

    /**
     * Cập nhật các chỉ số sinh tồn: energy, hydration, health
     */
    protected void updateSurvivalStats(float deltaTime) {
        if (this.currentState != AnimalState.EATING) {
            this.energy = AnimalBioLogic.calculateEnergy(this.energy, this.hungerRate, deltaTime);
        }
        this.hydration = AnimalBioLogic.calculateHydration(this.hydration, this.thirstRate, deltaTime);
        this.health = AnimalBioLogic.calculateHealth(this.health, this.maxHealth, this.energy, this.hydration, this.healthDecayRate, deltaTime, this.currentState);
    }

    /**
     * Cập nhật trạng thái AI của động vật sử dụng State Machine
     */
    protected void updateAIState(float deltaTime) {
        stateTimer += deltaTime;
        decisionTimer += deltaTime;
        
        // Đưa ra quyết định mới mỗi 0.5 giây để tránh lag thuật toán
        if (decisionTimer > 0.5f) {
            decisionTimer = 0;
            AnimalState newState = makeDecision();
            
            if (newState != currentState) {
                previousState = currentState;
                currentState = newState;
                stateTimer = 0;
                onStateChanged(previousState, newState);
            }
        }
    }

    /**
     * Quyết định trạng thái tiếp theo dựa trên các điều kiện (Có thể ghi đè)
     */
    protected AnimalState makeDecision() {
        if (currentState == AnimalState.EATING || currentState == AnimalState.DRINKING) {
            if (stateTimer < 2.0f) return currentState; // Đang ăn/uống thì ăn nốt 2 giây
        }
        
        // Ưu tiên đi tìm nước nếu hydration < 50
        if (hydration < 50f) {
            return AnimalState.SEARCHING_WATER;
        }
        
        // Ưu tiên đi tìm thức ăn nếu energy < 60
        if (energy < 60f) {
            return AnimalState.SEARCHING_FOOD;
        }
        
        return AnimalState.WANDERING;
    }

    protected void onStateChanged(AnimalState oldState, AnimalState newState) {}

    /**
     * Di chuyển động vật dựa trên trạng thái hiện tại bằng A*
     */
    protected void move(float deltaTime) {
        Vector2 direction = new Vector2(0, 0);
        boolean hasTarget = false;
        Vector2 activeTarget = null;
        
        switch (currentState) {
            case SEARCHING_WATER:
                activeTarget = com.ecosystem.sim.util.ResourceTracker.getInstance().findNearestWater(position);
                if (activeTarget != null) {
                    setTargetPosition(activeTarget);
                    hasTarget = true;
                    
                    // Nếu đã đến gần hồ nước, tiến hành uống nước
                    if (position.dst(activeTarget) < 24f) {
                        drink(100f); // Uống nước hồi đầy 100% hydration
                    }
                } else {
                    direction = getWanderingDirection();
                }
                break;
            case SEARCHING_FOOD:
                if (targetPosition.len() > 0) {
                    activeTarget = targetPosition;
                    hasTarget = true;
                } else {
                    direction = getWanderingDirection();
                }
                break;
            case HUNTING:
                if (targetAnimal != null && targetAnimal.isAlive()) {
                    setTargetPosition(targetAnimal.getPosition());
                    activeTarget = targetPosition;
                    hasTarget = true;
                } else {
                    direction = getWanderingDirection();
                }
                break;
            case FLEEING:
                if (targetPosition.len() > 0) {
                    activeTarget = targetPosition;
                    hasTarget = true;
                } else {
                    direction = getWanderingDirection();
                }
                break;
            case WANDERING:
                if (currentPath.isEmpty() || pathIndex >= currentPath.size()) {
                    Vector2 wanderTarget = findRandomWanderTarget();
                    if (wanderTarget != null) {
                        setTargetPosition(wanderTarget);
                    }
                }
                if (targetPosition.len() > 0) {
                    activeTarget = targetPosition;
                    hasTarget = true;
                } else {
                    direction = getWanderingDirection();
                }
                break;
            case IDLE:
            case EATING:
            case DRINKING:
            case RESTING:
            case REPRODUCING:
                direction.set(0, 0);
                currentPath.clear();
                pathIndex = 0;
                break;
            default:
                break;
        }
        
        // Quản lý và tính toán đường đi A*
        if (hasTarget && activeTarget != null) {
            pathRecalcTimer += deltaTime;
            boolean needRecalc = currentPath.isEmpty() || 
                                 (pathIndex >= currentPath.size()) ||
                                 (activeTarget.dst(pathTargetPos) > 12f) ||
                                 (pathRecalcTimer > 0.4f);
            
            if (needRecalc) {
                pathRecalcTimer = 0f;
                pathTargetPos.set(activeTarget);
                currentPath = PathFinding.findAStarPath(position, activeTarget, mapManager, ignoreWater);
                pathIndex = 0;
            }
            
            // Đi dọc theo đường đi A* đã tìm
            if (!currentPath.isEmpty() && pathIndex < currentPath.size()) {
                Vector2 waypoint = currentPath.get(pathIndex);
                
                // Nếu đủ gần waypoint hiện tại, chuyển sang waypoint tiếp theo
                float distToWaypoint = position.dst(waypoint);
                if (distToWaypoint < 6f) {
                    pathIndex++;
                    if (pathIndex < currentPath.size()) {
                        waypoint = currentPath.get(pathIndex);
                    }
                }
                
                if (pathIndex < currentPath.size()) {
                    direction.set(waypoint).sub(position);
                } else {
                    direction.set(activeTarget).sub(position);
                }
            } else {
                direction.set(activeTarget).sub(position);
            }
        }
        
        if (direction.len() > 0) {
            direction.nor();
            direction.scl(speed * deltaTime);
            
            Vector2 newPos = new Vector2(position).add(direction);
            if (!isObstructed(newPos)) {
                position.add(direction);
                
                // Anti-stuck check: nếu di chuyển được, reset stuck timer
                float distMoved = position.dst(lastPosition);
                if (distMoved > 0.1f) {
                    stuckTimer = 0f;
                } else {
                    stuckTimer += deltaTime;
                }
                lastPosition.set(position);
            } else {
                stuckTimer += deltaTime;
                if (currentState == AnimalState.WANDERING) {
                    stateChangeTimer = 3.1f;
                    currentPath.clear();
                    pathIndex = 0;
                }
            }
            
            // Xử lý chống kẹt: nếu bị kẹt tại chỗ quá 0.8 giây
            if (stuckTimer > 0.8f) {
                currentPath.clear();
                pathIndex = 0;
                
                // Thử né theo một hướng ngẫu nhiên khác
                float randomAngle = MathUtils.random(360);
                Vector2 escapeDir = new Vector2(1, 0).setAngleDeg(randomAngle).scl(speed * deltaTime * 1.5f);
                Vector2 escapePos = new Vector2(position).add(escapeDir);
                
                if (!isObstructed(escapePos)) {
                    position.set(escapePos);
                    stuckTimer = 0f;
                } else {
                    Vector2 emptySpot = PathFinding.findNearbyEmptySpot(position, mapManager, 24);
                    if (emptySpot != null) {
                        position.set(emptySpot);
                    }
                    stuckTimer = 0f;
                }
            }
        } else {
            stuckTimer = 0f;
        }
        
        clampPositionToMapBounds();
        
        velocity.set(direction).scl(1f / (deltaTime > 0 ? deltaTime : 0.016f));
    }

    /**
     * Giữ animals trong ranh giới của map
     */
    private void clampPositionToMapBounds() {
        float mapWidth = 800f;   // 50 tiles × 16px
        float mapHeight = 800f;
        
        position.x = Math.max(0, Math.min(mapWidth - width, position.x));
        position.y = Math.max(0, Math.min(mapHeight - height, position.y));
    }

    protected Vector2 getWanderingDirection() {
        // Đổi hướng ngẫu nhiên sau mỗi 3 giây
        if (stateChangeTimer > 3.0f) {
            stateChangeTimer = 0;
            float angle = MathUtils.random(360);
            velocity = new Vector2(1, 0).setAngleDeg(angle);
        } else {
            stateChangeTimer += Gdx.graphics.getDeltaTime();
        }
        
        // Trả về hướng hiện tại (đã normalize)
        Vector2 direction = new Vector2(velocity);
        if (direction.len() > 0) {
            direction.nor();
        } else {
            // Fallback nếu velocity = (0,0)
            float angle = MathUtils.random(360);
            direction = new Vector2(1, 0).setAngleDeg(angle);
            velocity.set(direction);
        }
        return direction;
    }


    protected boolean isObstructed(Vector2 newPos) {
        // Kiểm tra va chạm toàn diện qua kiểm tra đè ô gạch của mapManager
        if (ignoreWater) {
            return mapManager.isAreaObstacleIgnoreWater(newPos.x, newPos.y, width, height);
        }
        return mapManager.isAreaObstacle(newPos.x, newPos.y, width, height);
    }

    protected boolean shouldDie() {
        return health <= 0 || age > 300f; // Chết khi hết máu hoặc sống quá 5 phút tuổi
    }

    public void eat(float foodValue) {
        energy = Math.min(maxEnergy, energy + foodValue);
        health = maxHealth; // Hồi phục 100% health khi ăn
        currentState = AnimalState.EATING;
        stateTimer = 0;
    }

    public void drink(float waterValue) {
        hydration = Math.min(maxHydration, hydration + waterValue);
        currentState = AnimalState.DRINKING;
        stateTimer = 0;
    }

    public void takeDamage(float damage) {
        health = Math.max(0, health - damage);
    }

    public void consumeAnimal(Animal prey) {
        if (prey != null && prey.isAlive()) {
            float meatValue = prey.bodySize * 20f;
            eat(meatValue);
            prey.die();
        }
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        AnimalViewLogic.draw(shapeRenderer, position, width, height, color, isAlive);
    }

    public abstract void cloneSelf();

    public abstract void specificBehavior(float deltaTime);


    /**
     * Chọn vị trí đích lang thang ngẫu nhiên và an toàn trên đất cỏ
     */
    protected Vector2 findRandomWanderTarget() {
        int attempts = 0;
        while (attempts < 20) {
            float angle = MathUtils.random(360f);
            float dist = MathUtils.random(60f, 150f);
            Vector2 candidate = new Vector2(position).add(new Vector2(dist, 0).rotateDeg(angle));
            if (candidate.x >= 0 && candidate.x < 800 && candidate.y >= 0 && candidate.y < 800) {
                if (!mapManager.isAreaObstacle(candidate.x, candidate.y, width, height) &&
                    mapManager.isGrass(candidate.x, candidate.y)) {
                    return candidate;
                }
            }
            attempts++;
        }
        return null;
    }

    // ============= GETTERS & SETTERS =============
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getEnergy() { return energy; }
    public float getHydration() { return hydration; }
    public float getSpeed() { return speed; }
    public int getBodySize() { return bodySize; }
    public int getDominance() { return dominance; }
    public float getSenseRadius() { return senseRadius; }
    public AnimalState getCurrentState() { return currentState; }
    public void setCurrentState(AnimalState state) { this.currentState = state; this.stateTimer = 0f; }
    public Animal getTargetAnimal() { return targetAnimal; }
    public Vector2 getTargetPosition() { return targetPosition; }

    public void setTargetAnimal(Animal target) { this.targetAnimal = target; }
    public void setTargetPosition(Vector2 pos) { this.targetPosition.set(pos); }
    public void setHealth(float value) { this.health = Math.min(maxHealth, value); }
    public void setEnergy(float value) { this.energy = Math.min(maxEnergy, value); }
    public void setHydration(float value) { this.hydration = Math.min(maxHydration, value); }
} 