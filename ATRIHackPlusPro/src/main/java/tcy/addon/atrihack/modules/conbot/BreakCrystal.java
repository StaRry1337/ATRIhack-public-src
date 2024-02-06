package tcy.addon.atrihack.modules.conbot;

import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.utils.SettingUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

public class BreakCrystal extends ArkModule {
    private Entity crystal;
    public BreakCrystal() {
        super(ATRIHack.atricombot, "BreakCrystal", "");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        crystal = this.getBlocking();
        if (crystal == null){return;}
        sendPacket(PlayerInteractEntityC2SPacket.attack(crystal,mc.player.isSneaking()));
        toggle();
    }

    private Entity getBlocking() {
        Entity crystal = null;
        double lowest = 1000;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (mc.player.distanceTo(entity) > 5) continue;
            if (!SettingUtils.inAttackRange(entity.getBoundingBox())) continue;
            crystal = entity;
        }
        return crystal;
    }
}
