package com.ecosystem.sim.util;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.ecosystem.sim.entity.Animal;
import com.ecosystem.sim.entity.Herbivore;
import com.ecosystem.sim.entity.AnimalState;
import com.ecosystem.sim.entity.Carnivore;
import com.ecosystem.sim.entity.Entity;
import com.ecosystem.sim.entity.Plant;
import com.ecosystem.sim.entity.concrete.Rabbit;
import com.ecosystem.sim.entity.concrete.Wolf;
import com.ecosystem.sim.entity.concrete.Deer;
import com.ecosystem.sim.entity.concrete.Tiger;
import com.ecosystem.sim.entity.concrete.Elephant;
import com.ecosystem.sim.entity.concrete.Grass;
import com.ecosystem.sim.entity.concrete.Tree;
import com.ecosystem.sim.EcoSim;
import com.ecosystem.sim.entity.behavior.IPredator;
import com.ecosystem.sim.entity.behavior.IPrey;
import com.ecosystem.sim.map.MapManager;

import java.util.*;

/**
 * Quản lý tất cả các thực thể trong thế giới
 */
public class EntityManager {
    private List<Entity> entities;
    private List<Animal> animals;
    private List<Plant> plants;
    private MapManager mapManager;
    private ZoneManager zoneManager;
    private List<Entity> entitiesToAdd;
    private boolean isUpdating = false;
    
    // Object Pools
    private Pool<Rabbit> rabbitPool;
    private Pool<Wolf> wolfPool;
    private Pool<Grass> grassPool;
    private Pool<Tree> treePool;
    private Pool<Deer> deerPool;
    private Pool<Tiger> tigerPool;
    private Pool<Elephant> elephantPool;
    
    // Singleton Instance
    private static EntityManager instance;
    public static EntityManager getInstance() { return instance; }
    
    // Season system
    private static Season currentSeason = Season.BREEDING;
    private static boolean isAutoCycleEnabled = true;
    private static float seasonTimeLeft = 30.0f; // 30 giây mỗi mùa
    
    public static Season getCurrentSeason() { return currentSeason; }
    public static void setCurrentSeason(Season season) { 
        currentSeason = season; 
        seasonTimeLeft = 30.0f; // Reset đếm ngược khi đổi mùa
    }
    public static boolean isAutoCycleEnabled() { return isAutoCycleEnabled; }
    public static void setAutoCycleEnabled(boolean enabled) { isAutoCycleEnabled = enabled; }
    public static float getSeasonTimeLeft() { return seasonTimeLeft; }
    

    
    public EntityManager(MapManager mapManager) {
        instance = this;
        this.entities = new ArrayList<>();
        this.animals = new ArrayList<>();
        this.plants = new ArrayList<>();
        this.entitiesToAdd = new ArrayList<>();
        this.isUpdating = false;

        this.mapManager = mapManager;
        this.zoneManager = new ZoneManager();
        
        // Khởi tạo ResourceTracker
        ResourceTracker.getInstance().initialize(mapManager);
        
        // Khởi tạo các Pools
        this.rabbitPool = new Pool<Rabbit>() {
            @Override protected Rabbit newObject() { return new Rabbit(0, 0, mapManager); }
        };
        this.wolfPool = new Pool<Wolf>() {
            @Override protected Wolf newObject() { return new Wolf(0, 0, mapManager); }
        };
        this.grassPool = new Pool<Grass>() {
            @Override protected Grass newObject() { return new Grass(0, 0, mapManager); }
        };
        this.treePool = new Pool<Tree>() {
            @Override protected Tree newObject() { return new Tree(0, 0, mapManager); }
        };
        this.deerPool = new Pool<Deer>() {
            @Override protected Deer newObject() { return new Deer(0, 0, mapManager); }
        };
        this.tigerPool = new Pool<Tiger>() {
            @Override protected Tiger newObject() { return new Tiger(0, 0, mapManager); }
        };
        this.elephantPool = new Pool<Elephant>() {
            @Override protected Elephant newObject() { return new Elephant(0, 0, mapManager); }
        };
    }

    /**
     * Spawn thực thể dùng Object Pool
     */
    public Rabbit spawnRabbit(float x, float y) {
        Rabbit rabbit = rabbitPool.obtain();
        rabbit.init(x, y);
        addEntity(rabbit);
        return rabbit;
    }

    public Wolf spawnWolf(float x, float y) {
        Wolf wolf = wolfPool.obtain();
        wolf.init(x, y);
        addEntity(wolf);
        return wolf;
    }

    public Deer spawnDeer(float x, float y) {
        Deer deer = deerPool.obtain();
        deer.init(x, y);
        addEntity(deer);
        return deer;
    }

    public Tiger spawnTiger(float x, float y) {
        Tiger tiger = tigerPool.obtain();
        tiger.init(x, y);
        addEntity(tiger);
        return tiger;
    }

    public Elephant spawnElephant(float x, float y) {
        Elephant elephant = elephantPool.obtain();
        elephant.init(x, y);
        addEntity(elephant);
        return elephant;
    }

    public Grass spawnGrass(float x, float y) {
        Grass grass = grassPool.obtain();
        grass.init(x, y);
        addEntity(grass);
        return grass;
    }

    public Tree spawnTree(float x, float y) {
        Tree tree = treePool.obtain();
        tree.init(x, y);
        addEntity(tree);
        return tree;
    }

    /**
     * Thêm một thực thể vào quản lý (chỉ dùng nội bộ hoặc nếu không dùng Pool)
     */
    public void addEntity(Entity entity) {
        // Kiểm tra giới hạn số lượng cá thể tối đa trước khi thêm để tránh bùng nổ dân số khi tự nhân bản
        if (entity instanceof Elephant) {
            if (getElephantCount() >= EcoSim.MAX_ELEPHANTS) return;
        } else if (entity instanceof Tiger) {
            if (getTigerCount() >= EcoSim.MAX_TIGERS) return;
        } else if (entity instanceof Wolf) {
            if (getWolfCount() >= EcoSim.MAX_WOLVES) return;
        } else if (entity instanceof Deer) {
            if (getDeerCount() >= EcoSim.MAX_DEERS) return;
        } else if (entity instanceof Rabbit) {
            if (getRabbitCount() >= EcoSim.MAX_RABBITS) return;
        } else if (entity instanceof Grass) {
            int maxGrass = currentSeason == Season.DROUGHT ? EcoSim.MAX_GRASS_DROUGHT : EcoSim.MAX_GRASS_BREEDING;
            if (getGrassCount() >= maxGrass) return;
        } else if (entity instanceof Tree) {
            int maxTrees = currentSeason == Season.DROUGHT ? EcoSim.MAX_TREES_DROUGHT : EcoSim.MAX_TREES_BREEDING;
            if (getTreeCount() >= maxTrees) return;
        }

        if (isUpdating) {
            entitiesToAdd.add(entity);
        } else {
            entities.add(entity);
            
            if (entity instanceof Animal) {
                animals.add((Animal) entity);
                if (entity instanceof Herbivore) {
                    ResourceTracker.getInstance().registerHerbivore((Animal) entity);
                }
            } else if (entity instanceof Plant) {
                plants.add((Plant) entity);
                ResourceTracker.getInstance().registerPlant((Plant) entity);
            }
        }
    }

    /**
     * Cập nhật tất cả thực thể
     */
    public void update(float deltaTime) {
        isUpdating = true;

        // Cập nhật chu kỳ mùa tự động (5 giây mỗi mùa)
        if (isAutoCycleEnabled) {
            seasonTimeLeft -= deltaTime;
            if (seasonTimeLeft <= 0) {
                setCurrentSeason(currentSeason == Season.BREEDING ? Season.DROUGHT : Season.BREEDING);
            }
        }

        // Cập nhật tất cả thực thể
        for (Entity entity : entities) {
            if (entity.isAlive()) {
                entity.update(deltaTime);
            }
        }
        
        // Cập nhật ZoneManager
        zoneManager.clear();
        for (Entity entity : entities) {
            if (entity.isAlive()) {
                ZoneManager.Zone zone = zoneManager.getZone(entity.getX(), entity.getY());
                if (entity instanceof Animal) {
                    zone.animals.add((Animal) entity);
                } else if (entity instanceof Plant) {
                    zone.plants.add((Plant) entity);
                }
            }
        }
        
        // Phát hiện va chạm và tương tác dùng ZoneManager
        updateInteractions();

        isUpdating = false;

        // Thêm các thực thể mới sinh ra trong quá trình cập nhật
        if (!entitiesToAdd.isEmpty()) {
            for (Entity e : entitiesToAdd) {
                entities.add(e);
                if (e instanceof Animal) {
                    animals.add((Animal) e);
                    if (e instanceof Herbivore) {
                        ResourceTracker.getInstance().registerHerbivore((Animal) e);
                    }
                } else if (e instanceof Plant) {
                    plants.add((Plant) e);
                    ResourceTracker.getInstance().registerPlant((Plant) e);
                }
            }
            entitiesToAdd.clear();
        }
        
        // Xóa các thực thể chết và trả về Pool
        for (int i = entities.size() - 1; i >= 0; i--) {
            Entity e = entities.get(i);
            if (!e.isAlive()) {
                if (e instanceof Herbivore) {
                    ResourceTracker.getInstance().unregisterHerbivore((Animal) e);
                }
                if (e instanceof Plant) {
                    ResourceTracker.getInstance().unregisterPlant((Plant) e);
                }
                
                if (e instanceof Rabbit) rabbitPool.free((Rabbit) e);
                else if (e instanceof Wolf) wolfPool.free((Wolf) e);
                else if (e instanceof Deer) deerPool.free((Deer) e);
                else if (e instanceof Tiger) tigerPool.free((Tiger) e);
                else if (e instanceof Elephant) elephantPool.free((Elephant) e);
                else if (e instanceof Grass) grassPool.free((Grass) e);
                else if (e instanceof Tree) treePool.free((Tree) e);
                entities.remove(i);
            }
        }
        animals.removeIf(a -> !a.isAlive());
        plants.removeIf(p -> !p.isAlive());
        

    }

    /**
     * Cập nhật tương tác giữa các thực thể dùng Spatial Partitioning (ZoneManager)
     */
    private void updateInteractions() {
        for (Animal animal : animals) {
            if (!animal.isAlive()) continue;

            List<ZoneManager.Zone> nearbyZones = zoneManager.getAdjacentZones(animal.getX(), animal.getY());
            
            // Lấy danh sách animals và plants gần kề
            List<Animal> nearbyAnimals = new ArrayList<>();
            List<Plant> nearbyPlants = new ArrayList<>();
            for (ZoneManager.Zone zone : nearbyZones) {
                nearbyAnimals.addAll(zone.animals);
                nearbyPlants.addAll(zone.plants);
            }

            // Sói/Hổ săn bắt
            if (animal instanceof IPredator) {
                IPredator hunter = (IPredator) animal;
                List<Entity> nearbyEntities = new ArrayList<>();
                nearbyEntities.addAll(nearbyAnimals);
                nearbyEntities.addAll(nearbyPlants);
                Animal prey = hunter.detectPrey(nearbyEntities);
                
                if (prey != null && prey instanceof IPrey) {
                    ((IPrey) prey).flee(animal);
                    hunter.hunt(prey);
                }
            }
            
            // Thỏ và Hươu tìm kiếm kẻ thù
            if (animal instanceof IPrey) {
                IPrey herbivore = (IPrey) animal;
                Animal threat = herbivore.detectThreat(nearbyAnimals);
                
                if (threat != null) {
                    herbivore.flee(threat);
                }
            }

            // Động vật ăn cỏ quét tìm thức ăn (cỏ thực thể)
            if (animal instanceof Herbivore && animal.getCurrentState() == AnimalState.SEARCHING_FOOD) {
                Herbivore herbivore = (Herbivore) animal;
                Plant food = herbivore.detectFood();
                if (food != null) {
                    herbivore.setTargetFood(food);
                }
            }
            
            // Động vật ăn thực vật (Động vật ăn thịt Carnivore tuyệt đối không ăn thực vật)
            if (animal instanceof Herbivore) {
                Herbivore herbivore = (Herbivore) animal;
                for (Plant plant : nearbyPlants) {
                    if (!plant.isAlive()) continue;
                    // Phân hóa thức ăn: chỉ ăn plant đúng ediblePlantType
                    if (herbivore.ediblePlantType.isInstance(plant)) {
                        if (animal.collidesWith(plant.getX(), plant.getY(), plant.getWidth(), plant.getHeight())) {
                            float foodValue = plant.getNutritionalValue();
                            if (foodValue > 0) {
                                animal.eat(foodValue);
                                plant.beEaten();
                            }
                        }
                    }
                }
            }
            
            // Xử lý va chạm sinh tồn và dominance
            for (Animal other : nearbyAnimals) {
                if (animal == other || !other.isAlive()) continue;
                if (animal.collidesWith(other.getX(), other.getY(), other.getWidth(), other.getHeight())) {
                    if (animal instanceof Herbivore && other instanceof Herbivore) {
                        // Cả hai là ăn cỏ: không gây hại, chỉ dạt ra nhường đường
                        if (animal.getDominance() < other.getDominance()) {
                            Vector2 away = PathFinding.findNearbyEmptySpot(animal.getPosition(), mapManager, 24);
                            animal.setTargetPosition(away);
                        }
                    } else if (animal instanceof Carnivore && other instanceof Herbivore) {
                        // Ăn thịt va chạm Ăn cỏ
                        Carnivore carnivore = (Carnivore) animal;
                        Herbivore herbivore = (Herbivore) other;
                        if (herbivore.getDominance() > carnivore.getDominance()) {
                            // Carnivore va phải Herbivore có dominance lớn hơn (Voi) -> mất 80% máu và bỏ chạy
                            if (carnivore.getCurrentState() != AnimalState.FLEEING) {
                                carnivore.takeDamage(carnivore.getMaxHealth() * 0.8f);
                                Vector2 fleeDir = new Vector2(carnivore.getPosition()).sub(herbivore.getPosition()).nor();
                                if (fleeDir.len() == 0) fleeDir.setToRandomDirection();
                                carnivore.setTargetPosition(new Vector2(carnivore.getPosition()).add(fleeDir.scl(150f)));
                                carnivore.setCurrentState(AnimalState.FLEEING);
                            }
                        }
                    } else if (animal instanceof Herbivore && other instanceof Carnivore) {
                        // Ăn cỏ va chạm Ăn thịt
                        Herbivore herbivore = (Herbivore) animal;
                        Carnivore carnivore = (Carnivore) other;
                        if (herbivore.getDominance() > carnivore.getDominance()) {
                            // Carnivore va phải Herbivore có dominance lớn hơn (Voi) -> mất 80% máu và bỏ chạy
                            if (carnivore.getCurrentState() != AnimalState.FLEEING) {
                                carnivore.takeDamage(carnivore.getMaxHealth() * 0.8f);
                                Vector2 fleeDir = new Vector2(carnivore.getPosition()).sub(herbivore.getPosition()).nor();
                                if (fleeDir.len() == 0) fleeDir.setToRandomDirection();
                                carnivore.setTargetPosition(new Vector2(carnivore.getPosition()).add(fleeDir.scl(150f)));
                                carnivore.setCurrentState(AnimalState.FLEEING);
                            }
                        }
                    } else if (animal instanceof Carnivore && other instanceof Carnivore) {
                        // Cả hai là Ăn thịt
                        Carnivore c1 = (Carnivore) animal;
                        Carnivore c2 = (Carnivore) other;
                        if (c1.getDominance() < c2.getDominance()) {
                            // c1 yếu hơn c2: c1 mất 40% máu và chạy trốn
                            if (c1.getCurrentState() != AnimalState.FLEEING) {
                                c1.takeDamage(c1.getMaxHealth() * 0.4f);
                                Vector2 fleeDir = new Vector2(c1.getPosition()).sub(c2.getPosition()).nor();
                                if (fleeDir.len() == 0) fleeDir.setToRandomDirection();
                                c1.setTargetPosition(new Vector2(c1.getPosition()).add(fleeDir.scl(150f)));
                                c1.setCurrentState(AnimalState.FLEEING);
                            }
                        }
                    }
                }
            }
        }
    }



    /**
     * Vẽ tất cả thực thể
     */
    public void render(ShapeRenderer shapeRenderer) {
        for (Entity entity : entities) {
            if (entity.isAlive()) {
                entity.render(shapeRenderer);
            }
        }
    }

    // ============= GETTERS =============

    private int countByType(Class<?> type) {
        int count = 0;
        for (Entity e : entities) {
            if (type.isInstance(e) && e.isAlive()) count++;
        }
        for (Entity e : entitiesToAdd) {
            if (type.isInstance(e) && e.isAlive()) count++;
        }
        return count;
    }
    
    public int getAnimalCount() { return countByType(Animal.class); }
    public int getPlantCount() { return countByType(Plant.class); }
    public int getRabbitCount() { return countByType(Rabbit.class); }
    public int getWolfCount() { return countByType(Wolf.class); }
    public int getDeerCount() { return countByType(Deer.class); }
    public int getTigerCount() { return countByType(Tiger.class); }
    public int getElephantCount() { return countByType(Elephant.class); }
    public int getGrassCount() { return countByType(Grass.class); }
    public int getTreeCount() { return countByType(Tree.class); }
}
