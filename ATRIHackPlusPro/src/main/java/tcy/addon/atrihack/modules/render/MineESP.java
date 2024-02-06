package tcy.addon.atrihack.modules.render;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.renderer.Renderer2D;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.utils.OLEPOSSUtils;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import tcy.addon.atrihack.utils.render.Renderer2DPlus;
import tcy.addon.atrihack.utils.world.BlockInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author OLEPOSSU
 */

public class MineESP extends ArkModule {
    public MineESP() {
        super(ATRIHack.atrirender, "Mine Render", "Renders a box at blocks being mined by other players.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgText = settings.createGroup("Text");


    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("Range")
        .description("Only renders inside this range.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );
    private final Setting<Double> maxTime = sgGeneral.add(new DoubleSetting.Builder()
        .name("Max Time")
        .description("Removes rendered box after this time.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 50)
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    public final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Color of the outline.")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<Boolean> text = sgRender.add(new BoolSetting.Builder()
        .name("RenderText")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> textScale = sgText.add(new DoubleSetting.Builder()
        .name("Text Scale")
        .description(".")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(0, 10)
        .visible(text::get)
        .build()
    );
    public final Setting<SettingColor> Tcolor = sgRender.add(new ColorSetting.Builder()
        .name("Textcolor")
        .description("Color of the sides.")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .visible(text::get)
        .build()
    );


    private final List<Render> renders = new ArrayList<>();
    Render render = null;
    BlockPos blockPos = null;
    BlockPos renderBoxOne=null;
    Box renderBoxTwo=null;
    FindItemResult ironPick;
    FindItemResult diamondPick;
    FindItemResult netheritePick;
    String name=null;
    private int breakTimer = 0;
    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null) return;

        if (render != null && contains()) render = null;

        renders.removeIf(r -> System.currentTimeMillis() > r.time + Math.round(maxTime.get() * 1000) || (render != null && r.id == render.id) || !OLEPOSSUtils.solid2(r.pos));

        if (render != null) {
            renders.add(render);
            render = null;
        }

        renders.forEach(r -> {

            double delta = Math.min((System.currentTimeMillis() - r.time) / (maxTime.get() * 1000d), 1);
            event.renderer.box(getBox(r.pos, getProgress(Math.min(delta * 4, 1))), getColor(sideColor.get(), 1 - delta), getColor(lineColor.get(), 1 - delta), shapeMode.get(), 0);
        });

    }
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (render != null && contains()) render = null;
        if (render == null || renderBoxOne == null || renders == null) return;
        if (render != null) {
            renders.add(render);
            render = null;
        }
        if(text.get()) {
            renders.forEach(r -> {
                Vector3d p1 = new Vector3d((Vector3fc) r.pos());
                if (NametagUtils.to2D(p1, textScale.get())) {
                    String progress;
                    NametagUtils.begin(p1);
                    TextRenderer.get().begin(1.0, false, true);
                    progress = Math.round(1000 * breakTimer) / delay() + "%";
                    if (Math.round(1000 * breakTimer) / delay() >= 100) progress = "Done!";
                    TextRenderer.get().render(progress, -TextRenderer.get().getWidth(progress) / 2.0, 0.0, Tcolor.get());
                    TextRenderer.get().end();
                    NametagUtils.end();
                }
            });
        }
    }
    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockBreakingProgressS2CPacket packet) {
            renderBoxOne = packet.getPos();
            render = new Render(packet.getPos(), packet.getEntityId(), System.currentTimeMillis());
            name = String.valueOf(packet.getEntityId());
        }
    }
    private int delay() {
        if (netheritePick.isHotbar()) {
            int slot = netheritePick.slot();
            ItemStack pick = mc.player.getInventory().getStack(slot);
            int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, pick);
            switch (eff) {
                case 0 -> {return 170;}
                case 1 -> {return 140;}
                case 2 -> {return 110;}
                case 3 -> {return 80;}
                case 4 -> {return 60;}
                case 5 -> {return 45;}
            }
        }
        if (diamondPick.isHotbar()) {
            int slot = diamondPick.slot();
            ItemStack pick = mc.player.getInventory().getStack(slot);
            int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, pick);
            switch (eff) {
                case 0 -> {return 190;}
                case 1 -> {return 151;}
                case 2 -> {return 118;}
                case 3 -> {return 85;}
                case 4 -> {return 61;}
                case 5 -> {return 46;}
            }
        }
        if (ironPick.isHotbar()) {
            int slot = ironPick.slot();
            ItemStack pick = mc.player.getInventory().getStack(slot);
            int eff = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, pick);
            switch (eff) {
                case 0 -> {return 836;}
                case 1 -> {return 627;}
                case 2 -> {return 457;}
                case 3 -> {return 315;}
                case 4 -> {return 220;}
                case 5 -> {return 160;}
            }
        }
        return 0;
    }
    private boolean contains() {
        for (Render r : renders) {
            if (r.id == render.id && r.pos.equals(render.pos)) return true;
        }
        return false;
    }

    private Color getColor(Color color, double delta) {
        return new Color(color.r, color.g, color.b, (int) Math.floor(color.a * delta));
    }

    private double getProgress(double delta) {
        return 1 - Math.pow(1 - (delta), 5);
    }

    private Box getBox(BlockPos pos, double progress) {
        return new Box(pos.getX() + 0.5 - progress / 2, pos.getY() + 0.5 - progress / 2,pos.getZ() + 0.5 - progress / 2, pos.getX() + 0.5 + progress / 2, pos.getY() + 0.5 + progress / 2, pos.getZ() + 0.5 + progress / 2);
    }
    public enum RenderMode{
        Normal,
        MidY,
        Mid,
        Box,
        Smooth
    }
    public enum TColorMode{
        Custom,
        Smart,
        simple
    }
    private record Render(BlockPos pos, int id, long time) {}
}
