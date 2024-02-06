/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package tcy.addon.atrihack.modules.player;

import meteordevelopment.meteorclient.events.entity.player.StartBreakingBlockEvent;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.mixins.INClientPlayerInteractionManagerMixin;

import java.util.Objects;

public class InstaMine extends Module {
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable(0, -1, 0);
    private Direction direction;
    BlockPos last;
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgText = settings.createGroup("Text");


    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Faces the blocks being mined server side.")
        .defaultValue(true)
        .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Renders a block overlay on the block being broken.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> armswing = sgRender.add(new BoolSetting.Builder()
        .name("armswing")
        .description("send armswing packet.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The color of the sides of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 10))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The color of the lines of the blocks being rendered.")
        .defaultValue(new SettingColor(204, 0, 0, 255))
        .build()
    );
    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Automatically switches to the appropriate tool when a block can be mined.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Vanilla speed multiplier.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Boolean> effectCheck = sgGeneral.add(new BoolSetting.Builder()
        .name("Effect Check")
        .description("Modifies mining speed depending on haste and mining fatigue.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwitchMode> pickAxeSwitchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Pickaxe Switch Mode")
        .description("Method of switching. InvSwitch is used in most clients.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    //-----------------------------------------text-------------------------------------------
    private final Setting<Double> scale = sgText.add(new DoubleSetting.Builder()
        .name("scale")
        .description(".")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<TColorMode> Tcolormode = sgText.add(new EnumSetting.Builder<TColorMode>()
        .name("TColorMode")
        .description("The mode to render in.")
        .defaultValue(TColorMode.Custom)
        .build()
    );
    private final Setting<SettingColor> Tcolor = sgText.add(new ColorSetting.Builder()
        .name("TextColor")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<Boolean> shadow = sgText.add(new BoolSetting.Builder()
        .name("Shadow")
        .description("Do text shadow render.")
        .defaultValue(true)
        .build()
    );
    private int switchToolDelay; // 切换工具的延迟时间
    private boolean isSwitching = false;
    private int originalSlot = -1;
    private double minedFor = 0;
    public InstaMine() {
        super(ATRIHack.ArkMode, "insta-mine", "Attempts to instantly mine blocks.");
        this.switchToolDelay = 5; // 默认设置为5秒
    }

    @Override
    public void onActivate() {
        last = new BlockPos(0, -128, 0);
    }

    @EventHandler
    private void onStartBreakingBlock(StartBreakingBlockEvent event) {
        direction = event.direction;
        blockPos.set(event.blockPos);
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (Objects.requireNonNull(mc.interactionManager).isBreakingBlock()) {
            last = ((INClientPlayerInteractionManagerMixin) mc.interactionManager).getCurrentBreakingPos();
        }
        if (last.getY() == -128) return;
        //
        double progress = getMineProgress();

        if (autoSwitch.get() && !isSwitching && shouldMine()) {
            int bestToolSlot = findBestToolSlot(blockPos);

            if (bestToolSlot != -1 && mc.player.getInventory().selectedSlot != bestToolSlot) {
                originalSlot = mc.player.getInventory().selectedSlot;
                mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.UP));
                if(Math.round(progress*100)>100) {
                    mc.player.getInventory().selectedSlot = bestToolSlot;
                }
                isSwitching = true;

            }
        } else if (isSwitching && mc.player.getInventory().selectedSlot == originalSlot) {
            mc.player.getInventory().selectedSlot = originalSlot;
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.SWAP_ITEM_WITH_OFFHAND, BlockPos.ORIGIN, Direction.UP));
            isSwitching = false;
        }

        //
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, last, direction));

        if (armswing.get()) mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
        if (Objects.requireNonNull(mc.interactionManager).isBreakingBlock()) {
            last = ((INClientPlayerInteractionManagerMixin) mc.interactionManager).getCurrentBreakingPos();
        }
        if (last.getY() == -128) return;

    }
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        boolean compatibility = false;
        if (blockPos == null) return;
        Vec3d rPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.7, blockPos.getZ() + 0.5);
        Vector3d p1 = new Vector3d(rPos.x, (rPos.y - scale.get() / 9.9) - scale.get() / 3.333, rPos.z);
        if (!NametagUtils.to2D(p1, scale.get(), true)) {
            return;
        }
        NametagUtils.begin(p1);
        TextRenderer font = TextRenderer.get();
        font.begin(scale.get());
        double progress = getMineProgress();
        String text = (Math.round(progress*100) + "%");
        if(Math.round(progress*100)>100){
            text="100%";
        }
        try{
            if(Tcolormode.get()== TColorMode.Custom){
                font.render(text, -(font.getWidth(text) / 2), -(font.getHeight()), Tcolor.get(), shadow.get());
            }
            if(Tcolormode.get()== TColorMode.Smart){
                if(progress*100<=20){
                    font.render(text, -(font.getWidth(text) / 2), -(font.getHeight()),new Color(255,63,82,200), shadow.get());
                }
                if(progress*100>20 && progress*100<=50){
                    font.render(text, -(font.getWidth(text) / 2), -(font.getHeight()),new Color(255,63,82,200), shadow.get());
                }
                if(progress*100>50 && progress*100<=80){
                    font.render(text, -(font.getWidth(text) / 2), -(font.getHeight()),new Color(255,63,82,200), shadow.get());
                }
                if(progress*100>80){
                    font.render(text, -(font.getWidth(text) / 2), -(font.getHeight()),new Color(255,63,82,200), shadow.get());
                }
            }
            if(Tcolormode.get()== TColorMode.simple){
                if(mc.world.getBlockState(blockPos).getBlock()== Blocks.AIR) {
                    font.render("borke", -(font.getWidth("broke") / 2), -(font.getHeight()), Tcolor.get(), shadow.get());
                }else{
                    font.render("Breaking", -(font.getWidth("Breaking") / 2), -(font.getHeight()), Tcolor.get(), shadow.get());
                }
            }

            font.end();
            NametagUtils.end();

        } catch (Exception e) {
        }
    }
    public double getMineProgress() {
        if (blockPos == null) return -1;
        return minedFor / getMineTicks(fastestSlot(), true);
    }
    private boolean shouldMine() {
        if (blockPos.getY() == -128) return false;
        if (!BlockUtils.canBreak(blockPos)) return false;
        return true;
    }
    private int fastestSlot() {
        int slot = -1;
        if (mc.player == null || mc.world == null) {
            return -1;
        }
        for (int i = 0; i < (pickAxeSwitchMode.get() == SwitchMode.Silent ? 9 : 35); i++) {
            if (this.blockPos != null) {
                if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(BlockPos.ofFloored(blockPos.toCenterPos()))) > mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(BlockPos.ofFloored(blockPos.toCenterPos()))))) {
                    slot = i;
                }
            }
        }
        return slot;
    }
    private float getTime(BlockPos pos, int slot, boolean speedMod) {
        BlockState state = mc.world.getBlockState(pos);
        float f = state.getHardness(mc.world, pos);
        if (f == -1.0F) {
            return 0.0F;
        } else {
            float i = !state.isToolRequired() || mc.player.getInventory().getStack(slot).isSuitableFor(state) ? 30 : 100;
            return getSpeed(state, slot, speedMod) / f / i;
        }
    }
    private float getMineTicks(int slot, boolean speedMod) {
        return slot == -1 ? slot : (float) (1 / (getTime(BlockPos.ofFloored(blockPos.toCenterPos()), slot, speedMod) * speed.get()));
    }
    private float getSpeed(BlockState state, int slot, boolean speedMod) {
        ItemStack stack = mc.player.getInventory().getStack(slot);
        float f = mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(state);
        if (f > 1.0) {
            int i = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, stack);
            if (i > 0 && !stack.isEmpty()) f += (float) (i * i + 1);
        }

        if (!speedMod) return f;


        if (effectCheck.get()) {
            if (StatusEffectUtil.hasHaste(mc.player)) {
                f *= 1.0 + (float) (StatusEffectUtil.getHasteAmplifier(mc.player) + 1) * 0.2F;
            }
            if (mc.player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
                f *= Math.pow(0.3, mc.player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier() + 1);
            }
        }

        return f;
    }
    private int findBestToolSlot(BlockPos blockPos) {
        int bestSlot = -1;
        double bestSpeed = -1;

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || !canMine(stack, blockPos)) continue;

            double speed = getMiningSpeedMultiplier(stack, blockPos);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }
    public enum SwitchMode {
        Silent,
        PickSilent,
        InvSwitch
    }

    private boolean canMine(ItemStack stack, BlockPos blockPos) {
        // 检查物品是否可以挖掘特定方块
        BlockState blockState = mc.world.getBlockState(blockPos);
        return stack.getMiningSpeedMultiplier(blockState) > 1.0;
    }

    private double getMiningSpeedMultiplier(ItemStack stack, BlockPos blockPos) {
        // 获取物品在挖掘特定方块时的挖掘速度
        BlockState blockState = mc.world.getBlockState(blockPos);
        return stack.getMiningSpeedMultiplier(blockState);
    }
    public enum TColorMode{
        Custom,
        Smart,
        simple
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get() || !shouldMine()) return;
        event.renderer.box(blockPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }

}
