package com.ecosystem.sim.entity;

/**
 * Trạng thái của động vật trong máy trạng thái
 * Quyết định hành động của động vật mỗi khung hình
 */
public enum AnimalState {
    IDLE,                // Đứng yên, không làm gì
    WANDERING,           // Đi dạo ngẫu nhiên
    SEARCHING_WATER,     // Tìm nước (hydration < 30%)
    SEARCHING_FOOD,      // Tìm thức ăn (energy < 40%)
    HUNTING,             // Truy đuổi con mồi (IPredator)
    FLEEING,             // Chạy thoát khỏi kẻ thù (IPrey)
    EATING,              // Đang ăn
    DRINKING,            // Đang uống nước
    RESTING,             // Đang nghỉ ngơi để phục hồi
    REPRODUCING,         // Đang sinh sản (nhân bản)
    DEAD                 // Chết
}
