package tcy.addon.atrihack.modules.player;

import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.text.Text;

public class FakePlayer extends ArkModule {
    public FakePlayer(){
        super(ATRIHack.ArkMode,"FakePlayer","");
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        mc.player.sendMessage(Text.of(".fake-player add atrihack"));
    }
    @Override
    public void onDeactivate() {
        mc.player.sendMessage(Text.of(".fake-player remove atrihack"));
    }
}
