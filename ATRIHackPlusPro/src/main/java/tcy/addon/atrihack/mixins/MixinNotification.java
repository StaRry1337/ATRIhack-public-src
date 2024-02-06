package tcy.addon.atrihack.mixins;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tcy.addon.atrihack.hud.ToastNotifications;

@Mixin(Modules.class)
public class MixinNotification {
    @Inject(method = "addActive", at = @At(value = "INVOKE", target = "Lmeteordevelopment/orbit/IEventBus;post(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private void onFuckOn(Module module, CallbackInfo ci) {
        //开启

        ToastNotifications.addToggled(module, " ON");
    }

    @Inject(method = "removeActive", at = @At(value = "INVOKE", target = "Lmeteordevelopment/orbit/IEventBus;post(Ljava/lang/Object;)Ljava/lang/Object;"), remap = false)
    private void onFuckOff(Module module, CallbackInfo ci) {
        //关闭

        ToastNotifications.addToggled(module, " OFF");
    }
}
