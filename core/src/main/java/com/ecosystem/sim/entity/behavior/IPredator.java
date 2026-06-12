package com.ecosystem.sim.entity.behavior;
import com.ecosystem.sim.entity.Animal;
import com.ecosystem.sim.entity.Entity;

public interface IPredator { //Giao diện cho các động vật ăn thịt (Sói, Hổ, Hổ báo, v.v.)
    void hunt(Animal prey);//Bắt đầu truy đuổi một con mồi
    Animal detectPrey(java.util.List<Entity> potentialPrey);// Trả về con mồi nếu phát hiện, hoặc null nếu không 
    float getHuntingRange();// Trả về bán kính tầm nhìn của con thú ăn thịt
}
