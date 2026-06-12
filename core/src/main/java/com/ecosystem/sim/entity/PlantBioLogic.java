package com.ecosystem.sim.entity;

/**
 * Lớp chịu trách nhiệm tính toán sinh học (survival stats, sinh trưởng, lão hóa)
 * Tách biệt hoàn toàn phần xử lý logic tăng trưởng khỏi phần vẽ (View) và thực thể (Entity)
 */
public class PlantBioLogic {

    /**
     * Lớp kết quả chứa các biến trạng thái mới sau khi tính toán sinh trưởng
     */
    public static class GrowthResult {
        public final Plant.GrowthStage stage;
        public final float progress;
        public final float rate;
        public final boolean shouldDie;

        public GrowthResult(Plant.GrowthStage stage, float progress, float rate, boolean shouldDie) {
            this.stage = stage;
            this.progress = progress;
            this.rate = rate;
            this.shouldDie = shouldDie;
        }
    }

    /**
     * Tính toán tiến độ sinh trưởng, chuyển giao giai đoạn (Seed -> Sprout -> Mature -> Withered)
     */
    public static GrowthResult updateGrowth(float deltaTime, float age, float wiltheringAge, 
                                            Plant.GrowthStage currentStage, float currentProgress, float currentRate) {
        float nextProgress = currentProgress + currentRate * deltaTime;
        Plant.GrowthStage nextStage = currentStage;
        float nextRate = currentRate;
        boolean shouldDie = false;

        // Chuyển sang giai đoạn tiếp theo
        if (nextProgress >= 100) {
            nextProgress = 0;
            switch (nextStage) {
                case SEED:
                    nextStage = Plant.GrowthStage.SPROUT;
                    break;
                case SPROUT:
                    nextStage = Plant.GrowthStage.MATURE;
                    break;
                case MATURE:
                    // Ở lại trưởng thành cho tới khi héo
                    nextProgress = 0;
                    nextRate = 0; // Không phát triển thêm
                    break;
                case WITHERED:
                    shouldDie = true;
                    break;
            }
        }

        // Kiểm tra điều kiện héo
        if (age >= wiltheringAge && nextStage != Plant.GrowthStage.WITHERED) {
            nextStage = Plant.GrowthStage.WITHERED;
            nextProgress = 0;
            nextRate = -10f; // Héo nhanh hơn
        }

        return new GrowthResult(nextStage, nextProgress, nextRate, shouldDie);
    }
}
