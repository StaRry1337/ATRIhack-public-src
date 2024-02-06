package tcy.addon.atrihack.hud;

import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.util.Identifier;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class HealthESP extends HudElement {
    public static final HudElementInfo<HealthESP> INFO = new HudElementInfo<>(ATRIHack.HUD_BLACKOUT, "HealthESP", "",HealthESP::new);

    public HealthESP(){
        super(INFO);
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    public final Setting<Boolean> chroma = sgGeneral.add(new BoolSetting.Builder().name("Chroma").description("Chroma logo animation.").defaultValue(false).build());
    private final Setting<Double> Scale = sgGeneral.add(new DoubleSetting.Builder().name("Scale").description("The scale of the logo.").defaultValue(1).min(0).sliderRange(0, 10).build());
    private final Setting<SideMode> side = sgGeneral.add(new EnumSetting.Builder<SideMode>().name("Kill Message Mode").description("What kind of messages to send.").defaultValue(SideMode.Right).build());
    private Identifier image;

    @Override
    public void render(HudRenderer renderer) {

        if(mc.player.getHealth()>=30){
            image = new Identifier("atrihack", "icons/hp/6.png");
        } else if (mc.player.getHealth() >= 24 && mc.player.getHealth()<30){
            image = new Identifier("atrihack", "icons/hp/5.png");
        } else if (mc.player.getHealth() >=18 && mc.player.getHealth()<24) {
            image = new Identifier("atrihack", "icons/hp/4.png");
        } else if (mc.player.getHealth() >=12 && mc.player.getHealth()<18) {
            image = new Identifier("atrihack", "icons/hp/3.png");
        } else if (mc.player.getHealth() >=6 && mc.player.getHealth()<12) {
            image = new Identifier("atrihack", "icons/hp/2.png");
        } else if (mc.player.getHealth() >=0 && mc.player.getHealth()<6) {
            image = new Identifier("atrihack", "icons/hp/1.png");
        }
        GL.bindTexture(image);
        Renderer2D.TEXTURE.begin();
        Renderer2D.TEXTURE.texQuad(x + (side.get() == HealthESP.SideMode.Left ? Scale.get() * 450 : 0),y, Scale.get() * (side.get() == HealthESP.SideMode.Left ? Scale.get() * -450 : 450), Scale.get() * 755, new Color(255, 255, 255, 255));
        Renderer2D.TEXTURE.render(null);
    }
    public enum SideMode {
        Right,
        Left
    }
}
