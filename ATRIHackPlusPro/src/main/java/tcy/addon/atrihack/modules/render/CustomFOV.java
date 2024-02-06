package tcy.addon.atrihack.modules.render;

import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import meteordevelopment.meteorclient.events.render.GetFovEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

/**
 * @author KassuK
 */

public class CustomFOV extends ArkModule {
    public CustomFOV() {
        super(ATRIHack.ArkMode, "Custom FOV", "Allows more customisation to the FOV.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> FOV = sgGeneral.add(new IntSetting.Builder()
        .name("FOV")
        .description("What the FOV should be.")
        .defaultValue(120)
        .range(0, 358)
        .sliderRange(0, 358)
        .build()
    );

    @EventHandler
    private void onFov(GetFovEvent event) {
        event.fov = FOV.get();
    }
}

