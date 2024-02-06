package tcy.addon.atrihack.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.notifications.Notification;
import tcy.addon.atrihack.notifications.NotificationsManager;
import tcy.addon.atrihack.notifications.events.ModuleToggledNotificationEvent;

import java.awt.*;

public class RemindESP extends ArkModule {
    public RemindESP(){
        super(ATRIHack.atrirender,"RemindESP","Remind players of various things");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> anchortrap = sgGeneral.add(new BoolSetting.Builder()
        .name("AnchorTrap")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> lowhealth = sgGeneral.add(new BoolSetting.Builder()
        .name("LowHealth")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> healthnv = sgGeneral.add(new DoubleSetting.Builder()
        .name("Health Numeric value")
        .description("The horizontal radius around you in which holes are rendered.")
        .defaultValue(7)
        .sliderMin(0.1)
        .sliderMax(20)
        .visible(lowhealth::get)
        .min(0)
        .build()
    );
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        if(anchortrap.get()){
            BlockPos a = new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+2,mc.player.getBlockZ());
            if (mc.player != null && mc.world.getBlockState(a).getBlock() == Blocks.RESPAWN_ANCHOR) {
                NotificationsManager.add(new Notification("You're being trapped by AnchorTrap!", Color.CYAN));
            }
        }

        if(lowhealth.get()){
            if(mc.player.getHealth()<=healthnv.get()){
                NotificationsManager.add(new Notification("Your HP is dangerous!", Color.yellow));
            }
        }
    }
}
