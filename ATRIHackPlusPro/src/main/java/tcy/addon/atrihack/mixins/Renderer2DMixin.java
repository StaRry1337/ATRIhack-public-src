package tcy.addon.atrihack.mixins;

import meteordevelopment.meteorclient.renderer.Renderer2D;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tcy.addon.atrihack.utils.notifications.DrawUtils;

/**
 * The dumbest possible way to do this, but it works.
 */
@Mixin(Renderer2D.class)
public class Renderer2DMixin {
	@Inject(method = "init", at = @At("HEAD"), remap = false)
	private static void doInject(CallbackInfo ci) {
		DrawUtils.init();
	}
}
