package tcy.addon.atrihack.modules.render;


import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.world.PlaySoundEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SoundRender extends ArkModule {
    public SoundRender() {
        super(ATRIHack.atrirender,"SoundRender","");
    }
    private BlockPos pos;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<List<SoundEvent>> bklist = sgGeneral.add(new SoundEventListSetting.Builder()
            .name("black-list")
            .description("Sounds to find.")
            .defaultValue(new ArrayList<>(0))
            .build()
    );
    private final Setting<Double> timeS = sgGeneral.add(new DoubleSetting.Builder()
            .name("Render Time")
            .description("The time between the sounds verification.")
            .defaultValue(0.6)
                    .min(0.1)
                    .max(2.0)
            .build()
    );
    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
            .name("Scale")
            .defaultValue(1)
            .sliderRange(0.1, 2)
            .build()
    );
    private final Setting<SettingColor> Tcolor = sgGeneral.add(new ColorSetting.Builder()
            .name("Text Color")
            .description(ATRIHack.COLOR)
            .defaultValue(new SettingColor(255, 255, 255))
            .build()
    );
    private List<Integer> delay = new ArrayList<Integer>();
    private List<bp> renderPos = new ArrayList<bp>();

    @EventHandler
    public void onPacket(PlaySoundEvent event){
        //for (SoundEvent sound : sounds.get()) {
        for(SoundEvent sounds:bklist.get()){
            if(event.sound.getId().equals(sounds.getId())){
                return;
            }
        }
        printSound(event.sound);

    }

    private void printSound(SoundInstance sound) {
        Vec3d pos = new Vec3d(sound.getX() - 0.5, sound.getY() - 0.5, sound.getZ() - 0.5);
        //if (!renderPos.contains(pos)) {
            //if(sound.getSound()!=null) {
                renderPos.add(new bp(pos,sound.toString(),System.currentTimeMillis()));
            //}
        //}
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        renderPos.forEach(bps-> {
            Vec3d pos=bps.pos;
            //if (pos == null) return;

            Vec3d rPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.7 +1, pos.getZ() + 0.5);
            Vector3d p1 = new Vector3d(rPos.x, (rPos.y - scale.get() / 9.9) - scale.get() / 3.333, rPos.z);
            if (!NametagUtils.to2D(p1, scale.get(), true)) {
                return;
            }
            NametagUtils.begin(p1);
            TextRenderer font = TextRenderer.get();
            font.begin(scale.get());
            String text = bps.text.replace("SoundInstance[","").replace("]","");

            try {
                font.render(text, -(font.getWidth(text) / 2), -(font.getHeight()), Tcolor.get(), false);
                font.end();
                NametagUtils.end();

            } catch (Exception e) {
            }
        });
        renderPos.removeIf(r -> System.currentTimeMillis() > r.time + timeS.get() * 1000);    }
    public record bp(Vec3d pos,String text,long time){};
}
