package tcy.addon.atrihack.modules.render;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.utils.EntityUtils;

import java.util.Map;

public class BurrowRender extends ArkModule {
    public BurrowRender(){
        super(ATRIHack.ArkMode,"BurrowESP","the block");
    }
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final Setting<Double> animationMoveExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Move Exponent")
        .description("Moves faster when longer away from the target.")
        .defaultValue(2)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered stuff")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    private final Setting<Boolean> renderWebbed = sgRender.add(new BoolSetting.Builder()
        .name("Render webbed")
        .description("Will render if the target is webbed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> webSideColor = sgRender.add(new ColorSetting.Builder()
        .name("web-side-color")
        .description("The side color of the rendering for webs.")
        .defaultValue(new SettingColor(240, 250, 65, 35))
        .visible(() -> renderWebbed.get())
        .build()
    );

    private final Setting<SettingColor> webLineColor = sgRender.add(new ColorSetting.Builder()
        .name("web-line-color")
        .description("The line color of the rendering for webs.")
        .defaultValue(new SettingColor(0, 0, 0, 0))
        .visible(() -> renderWebbed.get())
        .build()
    );
    private final Setting<Boolean> rendertext = sgRender.add(new BoolSetting.Builder()
        .name("Render text")
        .description("Will render if the target is webbed.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SettingColor> textColor = sgRender.add(new ColorSetting.Builder()
        .name("Text Color")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> rendertext.get())
        .build()
    );
    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts should be renderer.")
        .defaultValue(ShapeMode.Both)
        .build()
    );


    public BlockPos target;
    public boolean isTargetWebbed;
    public boolean isTargetBurrowed;
    @EventHandler
    private void onTick(TickEvent.Post event) {
        PlayerEntity targetEntity = TargetUtils.getPlayerTarget(mc.interactionManager.getReachDistance() + 2, SortPriority.LowestDistance);

        if (TargetUtils.isBadTarget(targetEntity, mc.interactionManager.getReachDistance() + 2)) {
            target = null;
        } else if (renderWebbed.get() && EntityUtils.isWebbed(targetEntity)) {
            target = targetEntity.getBlockPos();
        } else if (EntityUtils.isBurrowed(targetEntity, EntityUtils.BlastResistantType.Any)) {
            target = targetEntity.getBlockPos();
        } else target = null;

        isTargetWebbed = (target != null && EntityUtils.isWebbed(targetEntity));
        isTargetBurrowed = (target != null && EntityUtils.isBurrowed(targetEntity, EntityUtils.BlastResistantType.Any));
    }
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (target != null || mc.world != null) {
            Vector3d vec3 = new Vector3d();
            if (target != null) {
                vec3.set(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            }
            if (isTargetWebbed){
                event.renderer.box(target, webSideColor.get(), webLineColor.get(), shapeMode.get(), 0);
            }
            if (isTargetBurrowed) {
                event.renderer.box(target, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
            }

        }
    }
    @EventHandler
    private void onRender2D(final Render2DEvent event) {
        if (target != null || mc.world != null) {
            if(target == null)return;
            final Vector3d pos = new Vector3d(target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
            NametagUtils.begin(pos);
            TextRenderer.get().begin(1.0, false, true);
            if (isTargetWebbed) {
                TextRenderer.get().render("Burrow", -TextRenderer.get().getWidth("Burrow") / 2.0, 0.0, textColor.get());
            }else if (isTargetBurrowed) {
                TextRenderer.get().render("Web", -TextRenderer.get().getWidth("Web") / 2.0, 0.0, textColor.get());
            }
            TextRenderer.get().end();
            NametagUtils.end();
        }
    }
    }

