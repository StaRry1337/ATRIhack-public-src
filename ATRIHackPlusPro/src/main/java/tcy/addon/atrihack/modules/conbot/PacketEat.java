package tcy.addon.atrihack.modules.conbot;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;

public class PacketEat extends ArkModule {
    public PacketEat() {
        super(ATRIHack.atricombot, "PacketEat", "Eat without action.");
    }
}
