package com.ecosystem.sim.util;

import com.ecosystem.sim.entity.Animal;
import com.ecosystem.sim.entity.Plant;

import java.util.*;

/**
 * Hệ thống quản lý vùng (Zone) để tối ưu hóa hiệu năng
 * Chia bản đồ thành lưới các zone, chỉ xử lý tương tác trong cùng zone hoặc zone kế cạnh
 */
public class ZoneManager {
    private static final int ZONE_SIZE = 128; // Kích thước mỗi zone (pixel)
    private Map<String, Zone> zones;
    
    public static class Zone {
        public int x, y;
        public List<Animal> animals = new ArrayList<>();
        public List<Plant> plants = new ArrayList<>();
        
        Zone(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        String getKey() {
            return x + "," + y;
        }

        public void clear() {
            animals.clear();
            plants.clear();
        }
    }
    
    public ZoneManager() {
        this.zones = new HashMap<>();
    }
    
    /**
     * Lấy zone mà vật thể đó thuộc về
     */
    public Zone getZone(float x, float y) {
        int zoneX = (int)(x / ZONE_SIZE);
        int zoneY = (int)(y / ZONE_SIZE);
        
        String key = zoneX + "," + zoneY;
        if (!zones.containsKey(key)) {
            zones.put(key, new Zone(zoneX, zoneY));
        }
        
        return zones.get(key);
    }
    
    /**
     * Lấy tất cả zone kế cạnh (8 hướng + zone hiện tại)
     */
    public List<Zone> getAdjacentZones(float x, float y) {
        List<Zone> adjacent = new ArrayList<>();
        int zoneX = (int)(x / ZONE_SIZE);
        int zoneY = (int)(y / ZONE_SIZE);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                String key = (zoneX + dx) + "," + (zoneY + dy);
                if (zones.containsKey(key)) {
                    adjacent.add(zones.get(key));
                }
            }
        }
        
        return adjacent;
    }
    
    /**
     * Xóa sạch entities trong tất cả các zone (dùng ở đầu mỗi frame)
     */
    public void clear() {
        for (Zone zone : zones.values()) {
            zone.clear();
        }
    }
}
