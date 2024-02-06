package tcy.addon.atrihack.modules.player;

import io.netty.buffer.Unpooled;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import tcy.addon.atrihack.ATRIHack;

import static net.minecraft.util.profiling.jfr.event.PacketEvent.Names.PACKET_ID;

public class PlayerDev extends Module {
    public PlayerDev(){
        super(ATRIHack.ArkMode,"Player-Dev","Hello World");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> pauseEat = addPauseEat(sgGeneral);
//    @EventHandler(priority = EventPriority.HIGHEST)
//    private void onTick(TickEvent.Pre event) {
//        if(pauseEat.get() && mc.player.isUsingItem()){
//            sendEatPacket();
//        }
//    }
    public Setting<Boolean> addPauseEat(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Pause Eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
        );
    }



//    public static void sendEatPacket(String foodName) {
//        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
//        buf.writeString(foodName);
//        ClientPlayNetworking.send(PACKET_ID, buf);
//        ClientPlayNetworkHandler
//    }
}
