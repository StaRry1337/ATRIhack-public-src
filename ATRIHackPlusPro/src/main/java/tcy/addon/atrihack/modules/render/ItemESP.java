/**
 * ItemESP Module
 */
package tcy.addon.atrihack.modules.render;


import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.utils.RenderUtils;

public class ItemESP extends ArkModule {
    private final SettingGroup sgcolor = settings.createGroup("Color");

    private final Setting<SettingColor> wColor = sgcolor.add(new ColorSetting.Builder()
        .name("Color")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
	public ItemESP() {
        super(ATRIHack.atrirender, "ItemESP", "Allows the player to see items with an ESP.");
    }

    @EventHandler
	public void onRender(MatrixStack matrixStack, float partialTicks) {
		for (Entity entity : mc.world.getEntities()) {
			if(entity instanceof ItemEntity) {
				RenderUtils.draw3DBox(matrixStack, entity.getBoundingBox(), Color.fromRGBA(wColor.get().r,wColor.get().g,wColor.get().b,wColor.get().a), 0.2f);
			}
		}
	}

}
