package tcy.addon.atrihack.modules.render;


import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import tcy.addon.atrihack.ATRIHack;

public class ChorusRender extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");



    private final Setting<Keybind> key = sgGeneral.add(new KeybindSetting.Builder()
            .name("Accept")
            .description("The key that teleports you to the current spot.")
            .defaultValue(Keybind.fromKey(-1))
            .build()
    );

    // Render


    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("Shape")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("Block-Side")
            .description("The side color of the target block rendering.")
            .defaultValue(new SettingColor(255, 255, 255, 10))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("Block-Line")
            .description("The line color of the target block rendering.")
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );

    private final Setting<SettingColor> tracerColor = sgRender.add(new ColorSetting.Builder()
            .name("line")
            .description("The color of the tracer.")
            .defaultValue(new SettingColor(255, 255, 255, 255))
            .build()
    );

    private int teleportId;
    private BlockPos bpos;
    private Vec3d pos;

    public ChorusRender() {
        super(ATRIHack.atrirender, "ChorusRender", "");
    }

    @Override
    public void onActivate() {
        teleportId = -1;
        bpos = null;
        pos = null;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (key.get().isPressed()) {
            if (teleportId != -1) {
                mc.getNetworkHandler().sendPacket(new TeleportConfirmC2SPacket(teleportId));
                teleportId = -1;
                bpos = null;
                pos = null;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST - 55)
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof PlayerPositionLookS2CPacket packet) ||  !(mc.player.getMainHandStack().getItem() == Items.CHORUS_FRUIT && mc.player.isUsingItem() && (mc.player.getMainHandStack().getItem().isFood() || mc.player.getOffHandStack().getItem().isFood()))) return;

        teleportId = packet.getTeleportId();

        //bpos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
        pos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());

        event.cancel();
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof TeleportConfirmC2SPacket packet && packet.getTeleportId() == teleportId) {
            event.cancel();
            teleportId = -1;
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
       if (pos != null) {
            event.renderer.box(pos.x - 0.25, pos.y, pos.z - 0.25, pos.x + 0.25, pos.y + 1.5, pos.z + 0.25, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, pos.getX(), pos.getY() + 0.75, pos.getZ(), tracerColor.get());
        }
    }
}
