package tcy.addon.atrihack.modules.render;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.utils.ExtrapolationUtils;

import java.util.HashMap;
import java.util.Map;

public class extrapolateESP extends ArkModule {
    public extrapolateESP(){
        super(ATRIHack.atrirender,"extrapolateESP","");
    }
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgExtrapolation = settings.createGroup("Extrapolation");
    private final Setting<Boolean> renderExt = sgRender.add(new BoolSetting.Builder()
        .name("Render Extrapolation")
        .description("Renders boxes at players' predicted positions.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> renderSelfExt = sgRender.add(new BoolSetting.Builder()
        .name("Render Self Extrapolation")
        .description("Renders box at your predicted position.")
        .defaultValue(false)
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    //--------------------Extrapolation--------------------//
    private final Setting<Integer> selfExt = sgExtrapolation.add(new IntSetting.Builder()
        .name("Self Extrapolation")
        .description("How many ticks of movement should be predicted for self damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation")
        .description("How many ticks of movement should be predicted for enemy damage checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> rangeExtrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Range Extrapolation")
        .description("How many ticks of movement should be predicted for attack ranges before placing.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> hitboxExtrapolation = sgExtrapolation.add(new IntSetting.Builder()
        .name("Hitbox Extrapolation")
        .description("How many ticks of movement should be predicted for hitboxes in placing checks.")
        .defaultValue(0)
        .range(0, 100)
        .sliderMax(20)
        .build()
    );
    private final Setting<Integer> extSmoothness = sgExtrapolation.add(new IntSetting.Builder()
        .name("Extrapolation Smoothening")
        .description("How many earlier ticks should be used in average calculation for extrapolation motion.")
        .defaultValue(2)
        .range(1, 20)
        .sliderRange(1, 20)
        .build()
    );
    private final Map<AbstractClientPlayerEntity, Box> extPos = new HashMap<>();
    private final Map<AbstractClientPlayerEntity, Box> extHitbox = new HashMap<>();
    private Vec3d rangePos = null;
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPost(TickEvent.Post event) {
        ExtrapolationUtils.extrapolateMap(extPos, player -> player == mc.player ? selfExt.get() : extrapolation.get(), player -> extSmoothness.get());
        ExtrapolationUtils.extrapolateMap(extHitbox, player -> hitboxExtrapolation.get(), player -> extSmoothness.get());

        Box rangeBox = ExtrapolationUtils.extrapolate(mc.player, rangeExtrapolation.get(), extSmoothness.get());
        if (rangeBox == null) rangePos = mc.player.getEyePos();
        else rangePos = new Vec3d((rangeBox.minX + rangeBox.maxX) / 2f, rangeBox.minY + mc.player.getEyeHeight(mc.player.getPose()), (rangeBox.minZ + rangeBox.maxZ) / 2f);

    }
    @EventHandler(priority = EventPriority.HIGHEST + 1)
    private void onRender3D(Render3DEvent event) {
        if (mc.player != null) {
            //Render extrapolation
            if (renderExt.get()) {
                extPos.forEach((name, bb) -> {
                    if (renderSelfExt.get() || !name.equals(mc.player)) {

                        event.renderer.box(bb, color.get(), lineColor.get(), shapeMode.get(), 0);
                    }
                });
            }
        }
    }
}
