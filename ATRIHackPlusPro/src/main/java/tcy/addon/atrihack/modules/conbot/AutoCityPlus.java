package tcy.addon.atrihack.modules.conbot;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.modules.player.AutoMine;

public class AutoCityPlus extends ArkModule {
    public AutoCityPlus(){
        super(ATRIHack.atricombot,"AutoCity++","uwu");
    }
    private final SettingGroup sg =settings.getDefaultGroup();
    private final SettingGroup sgRender =settings.getDefaultGroup();
    private final Setting<Integer> range = sg.add(new IntSetting.Builder().name("Range").defaultValue(5).min(0).max(8).description("Mine Range.").build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("Line Color").description("Line color of rendered boxes").defaultValue(new SettingColor(255, 0, 0, 255)).build());
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder().name("Side Color").description("Side color of rendered boxes").defaultValue(new SettingColor(255, 0, 0, 50)).build());
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder().name("Fade Time").description("How long the fading should take.").defaultValue(1).min(0).sliderRange(0, 10).build());
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder().name("Render Time").description("How long the box should remain in full alpha value.").defaultValue(0.3).min(0).sliderRange(0, 10).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("Shape Mode").description("Which parts of render should be rendered.").defaultValue(ShapeMode.Both).build());
    private double renderProgress = 0;
    private Vec3d renderpos= null;
    private Vec3d renderpos2= null;
    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;
        if (!Modules.get().get(AutoMine.class).isActive()) return;
            renderProgress = fadeTime.get() + renderTime.get();
            for (Entity target : mc.world.getEntities()) {
            if (!(target instanceof PlayerEntity)) continue;
            if (mc.player.distanceTo(target) > range.get()) continue;
            if (Friends.get().isFriend((PlayerEntity) target)) continue;
            BlockPos targetPos = target.getBlockPos();
            if (mc.world.isAir(targetPos)) {
                if (targetPos.equals(Modules.get().get(AutoMine.class).targetPos())) {
                    Modules.get().get(AutoMine.class).onStart(targetPos);
                }
            }
                for (Direction dir : Direction.values()) {
                renderpos= new Vec3d(targetPos.getX(),targetPos.getY(),targetPos.getZ());
                    if (dir == Direction.DOWN || dir == Direction.UP) continue;
                if (mc.world.isAir(targetPos.offset(dir))) break;
                if (targetPos.offset(dir).equals(Modules.get().get(AutoMine.class).targetPos())) break;
                Modules.get().get(AutoMine.class).onStart(targetPos.offset(dir));
                break;
            }
                if (renderpos == null || renderpos2 == null)return;
                event.renderer.box(new Box(renderpos.getX(), renderpos.getY(), renderpos.getZ(),
                    renderpos.getX() + 1, renderpos.getY(), renderpos.getZ() + 1),
                    new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, renderProgress / fadeTime.get()))),
                    new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, renderProgress / fadeTime.get()))), shapeMode.get(), 0);
            }
    }
}
