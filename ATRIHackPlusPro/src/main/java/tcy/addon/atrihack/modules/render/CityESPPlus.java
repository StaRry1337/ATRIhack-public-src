package tcy.addon.atrihack.modules.render;


import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.utils.EntityUtils;

public class CityESPPlus extends ArkModule {
    private final SettingGroup sgRender = settings.createGroup("Render");


    // Render
    private final Setting<Double> range = sgRender.add(new DoubleSetting.Builder()
            .name("range")
            .description("The distance which to find the enemy.")
            .defaultValue(6)
            .range(0,20)
            .sliderRange(0,20)
            .build()
    );

    private final Setting<Boolean> prioBurrowed = sgRender.add(new BoolSetting.Builder()
            .name("prioritise-burrow")
            .description("Will prioritise rendering the burrow block.")
            .defaultValue(false)
            .build()
    );

    private final Setting<Boolean> noRenderSurrounded = sgRender.add(new BoolSetting.Builder()
            .name("not-surrounded")
            .description("Will not render if the target is not surrounded.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> avoidSelf = sgRender.add(new BoolSetting.Builder()
            .name("avoid-self")
            .description("Will avoid targeting self surround.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> lastResort = sgRender.add(new BoolSetting.Builder()
            .name("last-resort")
            .description("Will try to target your own surround as final option.")
            .defaultValue(true)
            .visible(avoidSelf::get)
            .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Sides)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color of the rendering.")
            .defaultValue(new SettingColor(255, 0, 0, 25))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color of the rendering.")
            .defaultValue(new SettingColor(255, 0, 0, 25))
            .build()
    );


    public CityESPPlus() {
        super(ATRIHack.atrirender, "city-esp+", "Display blocks that can be citied around your target.");
    }


    public BlockPos target;


    @EventHandler
    private void onTick(TickEvent.Post event) {
        PlayerEntity targetEntity = TargetUtils.getPlayerTarget(range.get(), SortPriority.LowestDistance);
        if(target == null)return;
        if (TargetUtils.isBadTarget(targetEntity, range.get())) {
            target = null;
        }
        if (prioBurrowed.get() && EntityUtils.isBurrowed(targetEntity, EntityUtils.BlastResistantType.Mineable)) {
            target = targetEntity.getBlockPos();
        }
        if (noRenderSurrounded.get() && !EntityUtils.isSurrounded(targetEntity, EntityUtils.BlastResistantType.Any)) {
            target = null;
        }
        if (avoidSelf.get()) {
            target = EntityUtils.getTargetBlock(targetEntity);
                if (target == null && lastResort.get()) target = EntityUtils.getCityBlock(targetEntity);
        } else target = EntityUtils.getCityBlock(targetEntity);
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target == null) return;
        event.renderer.box(target, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}
