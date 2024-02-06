package tcy.addon.atrihack.mixins;

import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tcy.addon.atrihack.utils.gui.FakeRiseGui;

@Mixin(TitleScreen.class)
public class MixinTitleScreen extends Screen {

    public MixinTitleScreen(Text title){
        super(title);
    }
    @Inject(method =  "tick",at = @At("HEAD"))
    private void tick(CallbackInfo ci){
        if(GuiThemes.get() != null){
            client.setScreen(new FakeRiseGui());
        }
    }
    @Inject(method =  "init",at = @At("HEAD"))
    private void init(CallbackInfo ci){
        if(GuiThemes.get() != null){
            client.setScreen(new FakeRiseGui());
        }
    }
}
