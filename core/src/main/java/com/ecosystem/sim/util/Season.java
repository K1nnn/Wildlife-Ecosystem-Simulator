package com.ecosystem.sim.util;

/**
 * Định nghĩa các mùa trong hệ sinh thái mô phỏng
 */
public enum Season {
    BREEDING("Breeding Season"),
    DROUGHT("Drought Season");

    public final String displayName;

    Season(String displayName) {
        this.displayName = displayName;
    }
}
