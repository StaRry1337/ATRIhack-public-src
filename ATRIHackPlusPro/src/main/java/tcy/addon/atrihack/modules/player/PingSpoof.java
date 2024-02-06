package tcy.addon.atrihack.modules.player;

import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import meteordevelopment.meteorclient.settings.*;

/**
 * @author OLEPOSSU
 */

public class PingSpoof extends ArkModule {
    public PingSpoof() {
        super(ATRIHack.ArkMode, "Ping Spoof", "Increases your ping.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Boolean> keepAlive = sgGeneral.add(new BoolSetting.Builder()
        .name("Keep Alive")
        .description("Delays keep alive packets.")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> pong = sgGeneral.add(new BoolSetting.Builder()
        .name("Pong")
        .description("Delays pong packets.")
        .defaultValue(false)
        .build()
    );
    public final Setting<Integer> ping = sgGeneral.add(new IntSetting.Builder()
        .name("Bonus Ping")
        .description("Increases your ping by this much.")
        .defaultValue(69)
        .min(0)
        .sliderRange(0, 1000)
        .build()
    );
}
