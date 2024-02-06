package tcy.addon.atrihack.events;

import net.minecraft.entity.Entity;


public class EventAttack  {
    boolean isCancelled = false;
    private Entity entity;
    public EventAttack(Entity entity){
        this.entity = entity;
    }

    public Entity getEntity(){
        return  entity;
    }
    public boolean isCancelled() {
        return isCancelled = true;
    }
}
