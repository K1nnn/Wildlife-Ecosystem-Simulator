package com.ecosystem.sim.entity;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;

/**
 * Lớp trừu tượng cơ sở cho tất cả các thực thể trong thế giới sinh thái
 * Mỗi thực thể có vị trí, khả năng cập nhật và hiển thị
 */
public abstract class Entity implements Pool.Poolable {
    protected Vector2 position;
    protected Vector2 velocity;
    protected float width;
    protected float height;
    
    // Lifecycle
    protected boolean isAlive;
    protected float age;

    public Entity(float x, float y, float width, float height) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(0, 0);
        this.width = width;
        this.height = height;
        this.isAlive = true;
        this.age = 0;
    }

    /**
     * Khởi tạo lại thực thể (dùng cho Object Pooling)
     */
    public void init(float x, float y) {
        this.position.set(x, y);
        this.velocity.set(0, 0);
        this.isAlive = true;
        this.age = 0;
    }

    @Override
    public void reset() {
        this.isAlive = false;
        this.age = 0;
        this.velocity.set(0, 0);
    }

    /**
     * Cập nhật trạng thái thực thể mỗi khung hình
     */
    public abstract void update(float deltaTime);

    /**
     * Vẽ thực thể lên màn hình
     */
    public abstract void render(ShapeRenderer shapeRenderer);

    /**
     * Kiểm tra xem thực thể có còn sống không
     */
    public boolean isAlive() {
        return isAlive;
    }

    /**
     * Giết thực thể
     */
    public void die() {
        this.isAlive = false;
    }

    /**
     * Kiểm tra xem thực thể có va chạm với một vùng hình chữ nhật không
     */
    public boolean collidesWith(float x, float y, float w, float h) {
        return position.x < x + w && 
               position.x + width > x && 
               position.y < y + h && 
               position.y + height > y;
    }

    // Getters
    public Vector2 getPosition() { return position; }
    public Vector2 getVelocity() { return velocity; }
    public float getX() { return position.x; }
    public float getY() { return position.y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getAge() { return age; }

    // Setters
    public void setPosition(float x, float y) { 
        position.set(x, y); 
    }
    public void setVelocity(float vx, float vy) { 
        velocity.set(vx, vy); 
    }
}
