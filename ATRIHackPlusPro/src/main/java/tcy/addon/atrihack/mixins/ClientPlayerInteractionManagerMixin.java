package tcy.addon.atrihack.mixins;


import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tcy.addon.atrihack.events.StopUsingItemEvent;
import tcy.addon.atrihack.modules.conbot.PacketEat;

import static meteordevelopment.meteorclient.MeteorClient.EVENT_BUS;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "stopUsingItem", at = @At("HEAD"), cancellable = true)
    private void stopUsingItem(PlayerEntity player, CallbackInfo ci) {
        StopUsingItemEvent event = new StopUsingItemEvent();
        EVENT_BUS.post(event);
        PacketEat p = Modules.get().get(PacketEat.class);
        if (event.isCancelled()) ci.cancel();
        if (p.isActive()){
            if (player.getActiveItem().getItem().equals(Items.ENCHANTED_GOLDEN_APPLE)){
                player.stopUsingItem();
                ci.cancel();
            }
        }
    }
}
