package com.ecosystem.sim.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/**
 * Quản lý tải và phát âm thanh trong game.
 */
public class SoundManager {
    private static Sound tigerHuntSound;
    private static Sound wolfHuntSound;
    private static boolean isInitialized = false;

    public static void init() {
        if (isInitialized) return;
        try {
            tigerHuntSound = Gdx.audio.newSound(Gdx.files.internal("tiger.mp3"));
            wolfHuntSound = Gdx.audio.newSound(Gdx.files.internal("wolf.mp3"));
            isInitialized = true;
        } catch (Exception e) {
            System.err.println("Không thể tải file âm thanh. Vui lòng kiểm tra lại thư mục assets/. " + e.getMessage());
        }
    }

    public static void playTigerHunt() {
        if (isInitialized && tigerHuntSound != null) {
            tigerHuntSound.play(0.5f); // Phát với âm lượng 50%
        }
    }

    public static void playWolfHunt() {
        if (isInitialized && wolfHuntSound != null) {
            wolfHuntSound.play(1f); // Phát với âm lượng 100%
        }
    }

    public static void dispose() {
        if (tigerHuntSound != null) {
            tigerHuntSound.dispose();
            tigerHuntSound = null;
        }
        if (wolfHuntSound != null) {
            wolfHuntSound.dispose();
            wolfHuntSound = null;
        }
        isInitialized = false;
    }
}
