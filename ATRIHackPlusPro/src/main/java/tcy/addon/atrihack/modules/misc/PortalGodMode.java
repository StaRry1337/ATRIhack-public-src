package tcy.addon.atrihack.modules.misc;

import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;

public class PortalGodMode extends ArkModule {
    public PortalGodMode() {super(ATRIHack.atrimisc, "Portal God Mode", "Prevents taking damage while in portals");}
    @EventHandler
    private void onSend(PacketEvent.Send event) {
        if (event.packet instanceof TeleportConfirmC2SPacket) {
            event.cancel();
        }
    }
}
