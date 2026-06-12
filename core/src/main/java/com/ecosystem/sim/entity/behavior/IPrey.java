package com.ecosystem.sim.entity.behavior;
import com.ecosystem.sim.entity.Animal;

public interface IPrey { //Giao diện cho các động vật bị săn (Thỏ, Hươu, v.v.)
    void flee(Animal predator); //Bắt đầu chạy thoát khỏi một kẻ thù
    Animal detectThreat(java.util.List<Animal> potentialThreats); //Kiểm tra xem có kẻ thù nào trong tầm nhìn không
    float getAwarenessRange();//Trả về bán kính tầm nhìn của con vật bị săn
}
