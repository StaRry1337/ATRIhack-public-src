package tcy.addon.atrihack.modules.player;


import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.joml.Vector3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.enums.RotationType;
import tcy.addon.atrihack.enums.SwingHand;
import tcy.addon.atrihack.globalsettings.SwingSettings;
import tcy.addon.atrihack.managers.Managers;
import tcy.addon.atrihack.modules.AnchorAuraPlus;
import tcy.addon.atrihack.utils.LemonUtils;
import tcy.addon.atrihack.utils.SettingUtils;
import tcy.addon.atrihack.utils.player.InventoryUtils;
import tcy.addon.atrihack.utils.world.BlockInfo;

import java.util.*;

public class AutoMine extends ArkModule {
    public AutoMine() {
        super(ATRIHack.ArkMode, "Auto Mine", "Automatically mines blocks to destroy your enemies.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgSpeed = settings.createGroup("Speed");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgText = settings.createGroup("Text");


    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause On Eat")
        .description("Pause while eating.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> pauseSword = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Sword")
        .description("Doesn't mine while holding sword.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SwitchMode> pickAxeSwitchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Pickaxe Switch Mode")
        .description("Method of switching. InvSwitch is used in most clients.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<SwitchMode> crystalSwitchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Crystal Switch Mode")
        .description("Method of switching. InvSwitch is used in most clients.")
        .defaultValue(SwitchMode.Silent)
        .build()
    );
    private final Setting<Boolean> manualMine = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Mine")
        .description("Sets target block to the block you clicked.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> manualInsta = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Instant")
        .description("Uses civ mine when mining manually.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> manualRemine = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Remine")
        .description("Mines the manually mined block again.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> fastRemine = sgGeneral.add(new BoolSetting.Builder()
        .name("Fast Remine")
        .description("Calculates mining progress from last block broken.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> manualRangeReset = sgGeneral.add(new BoolSetting.Builder()
        .name("Manual Range Reset")
        .description("Resets manual mining if out of range.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> resetOnSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("Reset On Switch")
        .description("Resets mining when switched held item.")
        .defaultValue(false)
        .build()
    );

    //--------------------Speed--------------------//
    private final Setting<Double> speed = sgSpeed.add(new DoubleSetting.Builder()
        .name("Speed")
        .description("Vanilla speed multiplier.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 2)
        .build()
    );
    private final Setting<Double> instaDelay = sgSpeed.add(new DoubleSetting.Builder()
        .name("Instant Delay")
        .description("Delay between civ mines.")
        .defaultValue(0.5)
        .min(0)
        .sliderRange(0, 1)
        .build()
    );
    private final Setting<Boolean> onGroundCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("On Ground Check")
        .description("Mines 5x slower when not on ground.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> effectCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Effect Check")
        .description("Modifies mining speed depending on haste and mining fatigue.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> waterCheck = sgSpeed.add(new BoolSetting.Builder()
        .name("Water Check")
        .description("Mines 5x slower while submerged in water.")
        .defaultValue(true)
        .build()
    );
    //--------------------Render--------------------//
    private final Setting<Boolean> mineStartSwing = sgRender.add(new BoolSetting.Builder()
        .name("Mine Start Swing")
        .description("Renders swing animation when starting mining.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> mineEndSwing = sgRender.add(new BoolSetting.Builder()
        .name("Mine End Swing")
        .description("Renders swing animation when ending mining.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> mineHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Mine Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(() -> mineStartSwing.get() || mineEndSwing.get())
        .build()
    );
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder()
        .name("Place Swing")
        .description("Renders swing animation when placing a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Place Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(placeSwing::get)
        .build()
    );
    private final Setting<Boolean> attackSwing = sgRender.add(new BoolSetting.Builder()
        .name("Attack Swing")
        .description("Renders swing animation when attacking a crystal.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> attackHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Attack Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(attackSwing::get)
        .build()
    );
    private final Setting<Double> animationExp = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Exponent")
        .description("3 - 4 look cool.")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<Double> scale = sgRender.add(new DoubleSetting.Builder()
        .name("scale")
        .description(".")
        .defaultValue(3)
        .range(0, 10)
        .sliderRange(0, 10)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("sideColor")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 0))
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("lineColor")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<SettingColor> sideColor2 = sgRender.add(new ColorSetting.Builder()
        .name("sideColor2")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 0))
        .build()
    );

    private final Setting<SettingColor> lineColor2 = sgRender.add(new ColorSetting.Builder()
        .name("lineColor2")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .build()
    );
    private final Setting<TColorMode> Tcolormode = sgText.add(new EnumSetting.Builder<TColorMode>()
        .name("TColorMode")
        .description("The mode to render in.")
        .defaultValue(TColorMode.Custom)
        .build()
    );
    private final Setting<RenderMode> rendermode = sgRender.add(new EnumSetting.Builder<RenderMode>()
        .name("Render Mode")
        .description("The mode to render in.")
        .defaultValue(RenderMode.Normal)
        .build()
    );
    private final Setting<FadeMode> fadeMode = sgRender.add(new EnumSetting.Builder<FadeMode>()
        .name("Fade Mode")
        .description("How long the fading should take.")
        .defaultValue(FadeMode.Normal)
        .visible(() -> rendermode.get() == RenderMode.Smooth)
        .build()
    );
    private final Setting<SettingColor> Tcolor = sgRender.add(new ColorSetting.Builder()
        .name("TextColor")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> Tcolormode.get().equals(TColorMode.Custom))
        .build()
    );
    private final Setting<Double> smoothness = sgRender.add(new DoubleSetting.Builder()
        .name("Smoothness")
        .description("How fast should boze mode box grow.")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> rendermode.get().equals(RenderMode.Smooth))
        .build()
    );
    private final Setting<Boolean> shadow = sgRender.add(new BoolSetting.Builder()
        .name("Shadow")
        .description("Do text shadow render.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Double> animationExponent = sgRender.add(new DoubleSetting.Builder()
        .name("Animation Exponent")
        .description("How fast should boze mode box grow.")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> rendermode.get().equals(RenderMode.Smooth))
        .build()
    );
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder()
        .name("renderTime")
        .description("How fast should boze mode box grow.")
        .defaultValue(3)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> rendermode.get().equals(RenderMode.Smooth))
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
    private double minedFor = 0;
    private double minedFor2 = 0;
    private Target target = null;
    private Target lasttarget = null;
    private Target target2 = null;
    private boolean started = false;
    private boolean started2 = false;
    private List<AbstractClientPlayerEntity> enemies = new ArrayList<>();
    private long lastTime = 0;
    private long lastTime2 = 0;
    private long lastPlace = 0;
    private double render = 1;
    private double render2 = 1;
    private double delta = 0;
    private double delta2 =0;

    private final Map<BlockPos, Long> explodeAt = new HashMap<>();
    private boolean reset = false;
    private boolean reset2 = false;
    private BlockState lastState = null;
    private BlockState lastState2 = null;
    private BlockPos lastPos = null;
    private BlockPos lastPos2 = null;
    BlockPos blockPos = null;
    BlockPos blockPos2 = null;
    Box renderBoxOne=null;
    Box renderBoxTwo=null;
    Box renderBoxOne2=null;
    Box renderBoxTwo2=null;
    private double renderProgress = 0;

    @Override
    public void onActivate() {
        target = null;
        target2 = null;
        lasttarget = null;
        minedFor = 0;
        started = false;
        started2 = false;
        lastTime = System.currentTimeMillis();
        reset = false;
        reset2 = false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onSend(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket && resetOnSwitch.get()) {
            reset = true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        if (mc.player == null || mc.world == null ||target2 == null) return;

        if (target != null) {
            if (lastState != null && target.pos.equals(lastPos) && target.manual && manualRemine.get() && !fastRemine.get() && !lastState.isSolid() && LemonUtils.solid2(target.pos)) {
                started = false;
            }
            renderProgress = Math.min(1, renderProgress + delta);
            lastPos = target.pos;
            lasttarget = target;
            lastState = mc.world.getBlockState(target.pos);
        } else {
            renderProgress = Math.max(0, renderProgress - delta);
            lastPos = null;
            lastState = null;
        }
        if (lasttarget==null) {
            lasttarget = target;
        }
        if (lasttarget==target2 && mc.world.getBlockState(target2.pos).isAir()) {
            lasttarget = target2 = null;
        }
        if(lasttarget!=target) {
            target2=lasttarget;
            if (target2 != null) {
                if (lastState2 != null && target2.pos.equals(lastPos2) && target2.manual && manualRemine.get() && !fastRemine.get() && !lastState2.isSolid() && LemonUtils.solid2(target2.pos)) {
                    started2 = false;
                }

                lastPos2 = target2.pos;
                lastState2 = mc.world.getBlockState(target2.pos);
            } else {
                lastPos2 = null;
                lastState2 = null;
            }
        }

        delta = (System.currentTimeMillis() - lastTime) / 1000d;
        lastTime = System.currentTimeMillis();

        delta2 = (System.currentTimeMillis() - lastTime2) / 1000d;
        lastTime2 = System.currentTimeMillis();
        update();


        if (target == null) return;
        if (target2 == null || target2==target) return;


        int slot = fastestSlot();

        render = MathHelper.clamp(getMineTicks(slot, true) == getMineTicks(slot, false) ? render + delta * 2 : render - delta * 2, -2, 2);

        render2 = MathHelper.clamp(getMineTicks(slot, true) == getMineTicks(slot, false) ? render2 + delta2 * 2 : render2 - delta2 * 2, -2, 2);

        blockPos = new BlockPos(target.pos);
        blockPos2 = new BlockPos(target2.pos);

        if(rendermode.get()==RenderMode.Smooth){

            if (renderTime.get() <= 0) return;

            if (renderBoxOne == null) renderBoxOne = new Box(blockPos);
            if (renderBoxOne2 == null) renderBoxOne2 = new Box(blockPos2);

            if (renderBoxTwo == null) {

                renderBoxTwo = new Box(blockPos);
            } else {
                ((IBox) renderBoxTwo).set(blockPos);
            }

            if (renderBoxTwo2 == null) {
                renderBoxTwo2 = new Box(blockPos2);
            } else {
                ((IBox) renderBoxTwo2).set(blockPos2);
            }

                double offsetX = (renderBoxTwo.minX - renderBoxOne.minX) / smoothness.get();
                double offsetY = (renderBoxTwo.minY - renderBoxOne.minY) / smoothness.get();
                double offsetZ = (renderBoxTwo.minZ - renderBoxOne.minZ) / smoothness.get();

                double offsetX2 = (renderBoxTwo2.minX - renderBoxOne2.minX) / smoothness.get();
                double offsetY2 = (renderBoxTwo2.minY - renderBoxOne2.minY) / smoothness.get();
                double offsetZ2 = (renderBoxTwo2.minZ - renderBoxOne2.minZ) / smoothness.get();

                ((IBox) renderBoxOne).set(
                    renderBoxOne.minX + offsetX,
                    renderBoxOne.minY + offsetY,
                    renderBoxOne.minZ + offsetZ,
                    renderBoxOne.maxX + offsetX,
                    renderBoxOne.maxY + offsetY,
                    renderBoxOne.maxZ + offsetZ
                );


                event.renderer.box(renderBoxOne, sideColor.get(), lineColor.get(), shapeMode.get(), 0);

                if (blockPos != blockPos2) {
                    ((IBox) renderBoxOne2).set(
                        renderBoxOne2.minX + offsetX2,
                        renderBoxOne2.minY + offsetY2,
                        renderBoxOne2.minZ + offsetZ2,
                        renderBoxOne2.maxX + offsetX2,
                        renderBoxOne2.maxY + offsetY2,
                        renderBoxOne2.maxZ + offsetZ2
                    );

                    double delta = Math.min((System.currentTimeMillis() - 100) / (maxTime.get() * 1000d), 1);
                    event.renderer.box(getBox(renderBoxOne2, getProgress(Math.min(delta * 4, 1))), sideColor2.get(), lineColor2.get(), shapeMode.get(), 0);

                }
        }

    }
    private Box getBox(Box pos, double progress) {
        return new Box(pos.minX + 0.5 - progress / 2, pos.minY + 0.5 - progress / 2,pos.minZ + 0.5 - progress / 2, pos.minX + 0.5 + progress / 2, pos.minY + 0.5 + progress / 2, pos.minZ + 0.5 + progress / 2);
    }
    private double getProgress(double delta) {
        return 1 - Math.pow(1 - (delta), 5);
    }
    @EventHandler
    public void onRender2D(Render2DEvent event) {
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
                if(mc.world.getBlockState(blockPos).getBlock()==Blocks.AIR) {
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
//
    public double getMineProgress() {
        if (target == null) return -1;
        return minedFor / getMineTicks(fastestSlot(), true);

    }

    private void update() {
        if (mc.world == null) return;

        if (reset) {
            if (target != null && !target.manual) {
                target = null;
            }
            started = false;
            reset = false;
        }

        if (reset2) {
            if (target2 != null && !target2.manual) {
                target2 = null;
            }
            started2 = false;
            reset2 = false;
        }

        enemies = mc.world.getPlayers().stream().filter(player -> player != mc.player && !Friends.get().isFriend(player) && player.distanceTo(mc.player) < 10).toList();

        BlockPos lastPos = target == null || target.pos == null ? null : target.pos;
        BlockPos lastPos2 = target2 == null || target2.pos == null ? null : target2.pos;

        if (target != null && target.manual && manualRangeReset.get() && !SettingUtils.inMineRange(target.pos)) {
            target = null;
            started = false;
        }

        if (target2 != null && target2.manual && manualRangeReset.get() && !SettingUtils.inMineRange(target2.pos)) {
            target2 = null;
            started2 = false;
        }

        if (target == null) return;

        if (lasttarget==null && target2 != lasttarget) {
            lasttarget = target;
        }

        if (target.pos != null && !target.pos.equals(lastPos)) {
            if (started) {
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, target.pos, Direction.DOWN, 0));
            }
            started = false;
        }
        if (!target.pos.equals(lasttarget.pos)) target2=lasttarget;
        if (target2.pos != null && !target2.pos.equals(lastPos2)) {
            if (started2) {
                sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, target2.pos, Direction.DOWN, 0));
            }
            started2 = false;
        }
        if (target2 == null) return;

        if (!started) {
            boolean rotated = !SettingUtils.startMineRot() || Managers.ROTATION.start(target.pos, priority, RotationType.Mining, Objects.hash(name + "mining"));

            if (rotated) {
                started = true;
                minedFor = 0;


                if (getMineTicks(fastestSlot(), true) == getMineTicks(fastestSlot(), false)) {
                    render = 2;
                } else {
                    render = -2;
                }

                Direction dir = SettingUtils.getPlaceOnDirection(target.pos);

                sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target.pos, dir == null ? Direction.UP : dir, s));

                SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);

                int slot = fastestSlot();
                BlockInfo.progress += BlockInfo.getBreakDelta(slot, mc.world.getBlockState(target.pos));

                if (mineStartSwing.get()) clientSwing(mineHand.get(), Hand.MAIN_HAND);

                if (SettingUtils.startMineRot()) {
                    Managers.ROTATION.end(Objects.hash(name + "mining"));
                }
            }
        }
        if (!started) return;
        if (target2 == target) return;
        if(lasttarget!=target) {
            target2 = lasttarget;
            if (!started2) {
                boolean rotated = !SettingUtils.startMineRot() || Managers.ROTATION.start(target2.pos, priority, RotationType.Mining, Objects.hash(name + "mining"));

                if (rotated) {
                    started2 = true;
                    minedFor2 = 0;


                    if (getMineTicks(fastestSlot(), true) == getMineTicks(fastestSlot(), false)) {
                        render2 = 2;
                    } else {
                        render2 = -2;
                    }

                    Direction dir = SettingUtils.getPlaceOnDirection(target2.pos);

                    sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, target2.pos, dir == null ? Direction.UP : dir, s));

                    SettingUtils.mineSwing(SwingSettings.MiningSwingState.Start);

                    int slot = fastestSlot();
                    BlockInfo.progress += BlockInfo.getBreakDelta(slot, mc.world.getBlockState(target2.pos));

                    if (mineStartSwing.get()) clientSwing(mineHand.get(), Hand.MAIN_HAND);

                    if (SettingUtils.startMineRot()) {
                        Managers.ROTATION.end(Objects.hash(name + "mining"));
                    }
                }
            }
            if (!started2) return;
        }
        minedFor += delta * 20;
        minedFor2 += delta2 * 20;
        if (isPaused()) return;
        if (!miningCheck(fastestSlot())) return;
        if (!LemonUtils.solid2(target.pos)) return;
        if (!LemonUtils.solid2(target2.pos)) return;

        if (lasttarget==target2 && mc.world.getBlockState(target2.pos).isAir()) {
            lasttarget = target2 = null;
        }
        endMine();
    }

    private boolean isPaused() {
        if (pauseEat.get() && mc.player.isUsingItem()) return true;
        if (pauseSword.get() && mc.player.getMainHandStack().getItem() instanceof SwordItem) return true;
        return false;
    }



    private void endMine() {
        int slot = fastestSlot();

        boolean switched = miningCheck(Managers.HOLDING.slot);
        boolean swapBack = false;

        Direction dir = SettingUtils.getPlaceOnDirection(target.pos);
        Direction dir2 = SettingUtils.getPlaceOnDirection(target2.pos);

        if (dir == null) {
            return;
        }

        if (dir2 == null) {
            return;
        }
        if (SettingUtils.shouldRotate(RotationType.Mining) && !Managers.ROTATION.start(target.pos, priority, RotationType.Mining, Objects.hash(name + "mining"))) {
            return;
        }
        if (SettingUtils.shouldRotate(RotationType.Mining) && !Managers.ROTATION.start(target2.pos, priority, RotationType.Mining, Objects.hash(name + "mining"))) {
            return;
        }
        if (!switched) {
            switch (pickAxeSwitchMode.get()) {
                case Silent -> {
                    switched = true;
                    InvUtils.swap(slot, true);
                }
                case PickSilent -> {
                    switched = true;
                    InventoryUtils.pickSwitch(slot);
                }
                case InvSwitch -> switched = InventoryUtils.invSwitch(slot);
            }
            swapBack = switched;
        }

        if (!switched) {
            return;
        }

        sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target.pos, dir, s));


        SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
        if (mineEndSwing.get()) clientSwing(mineHand.get(), Hand.MAIN_HAND);

        sendSequenced(s -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, target2.pos, dir2, s));

        SettingUtils.mineSwing(SwingSettings.MiningSwingState.End);
        if (mineEndSwing.get()) clientSwing(mineHand.get(), Hand.MAIN_HAND);

        if (SettingUtils.endMineRot()) {
            Managers.ROTATION.end(Objects.hash(name + "mining"));
        }

        if (swapBack) {
            switch (pickAxeSwitchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> InventoryUtils.pickSwapBack();
                case InvSwitch -> InventoryUtils.swapBack();
            }
        }


    }
    private void abort(BlockPos pos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
            pos, Direction.UP));

        started = false;
    }
    private void abort2(BlockPos pos) {
        mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK,
            pos, Direction.UP));

        started2 = false;
    }
    private Block getBlock(BlockPos pos) {
        return mc.world.getBlockState(pos).getBlock();
    }

    private Hand getHand() {
        if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
            return Hand.OFF_HAND;
        }
        if (Managers.HOLDING.isHolding(Items.END_CRYSTAL)) {
            return Hand.MAIN_HAND;
        }
        return null;
    }

    private boolean miningCheck(int slot) {
        if (target == null || target.pos == null) {
            return false;
        }
        return minedFor * speed.get() >= getMineTicks(slot, true);
    }
    private boolean miningCheck2(int slot) {
        if (target2 == null || target2.pos == null) {
            return false;
        }
        return minedFor2 * speed.get() >= getMineTicks(slot, true);
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
        return slot == -1 ? slot : (float) (1 / (getTime(target.pos, slot, speedMod) * speed.get()));
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

        if (waterCheck.get() && mc.player.isSubmergedInWater() && !EnchantmentHelper.hasAquaAffinity(mc.player)) {
            f /= 5.0;
        }

        if (onGroundCheck.get() && !mc.player.isOnGround()) {
            f /= 5.0;
        }

        return f;
    }
    public void onStart(BlockPos pos) {

        if (target != null && target.manual && pos.equals(target.pos)) {
            abort(target.pos);
            target = null;

            return;
        }
        if (manualMine.get() && getBlock(pos) != Blocks.BEDROCK) {
            started = false;

            target = new Target(pos, null, MineType.Manual, 0, manualInsta.get(), true);
        }
        if (lasttarget==null) {
            lasttarget = target;
        }
        if (lasttarget==target2 && mc.world.getBlockState(target2.pos).isAir()) {
            lasttarget = target2 = null;
        }
        if (target2 != null && target2.manual && pos.equals(target2.pos)) {
            abort2(target2.pos);
            target2 = null;
            return;
        }
        if (manualMine.get() && getBlock(pos) != Blocks.BEDROCK) {
            started2 = false;
            target2 = new Target(pos, null, MineType.Manual, 0, manualInsta.get(), true);
        }

    }

    public void onAbort(BlockPos pos) {
    }

    public void onStop(BlockPos position) {
        target = null;
        started = false;
        target2 = null;
        started2 = false;
    }

    private int fastestSlot() {
        int slot = -1;
        if (mc.player == null || mc.world == null) {
            return -1;
        }
        for (int i = 0; i < (pickAxeSwitchMode.get() == SwitchMode.Silent ? 9 : 35); i++) {
            if (this.target != null) {
                if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(target.pos)) > mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(target.pos)))) {
                    slot = i;
                }
            }
            if (this.target2 != null) {
                if (slot == -1 || (mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(mc.world.getBlockState(target2.pos)) > mc.player.getInventory().getStack(slot).getMiningSpeedMultiplier(mc.world.getBlockState(target2.pos)))) {
                    slot = i;
                }
            }
        }
        return slot;
    }

    private Color getColor(Color start, Color end, double progress, double alphaMulti) {
        return new Color(
            lerp(start.r, end.r, progress, 1),
            lerp(start.g, end.g, progress, 1),
            lerp(start.b, end.b, progress, 1),
            lerp(start.a, end.a, progress, alphaMulti));
    }

    private int lerp(double start, double end, double d, double multi) {
        return (int) Math.round((start + (end - start) * d) * multi);
    }

    public BlockPos targetPos() {
        return target == null ? null : target.pos;
    }

    private BlockPos getPos(Vec3d vec) {
        return new BlockPos((int) Math.floor(vec.x), (int) Math.round(vec.y), (int) Math.floor(vec.z));
    }
    public enum FadeMode {
        Up,
        Down,
        Normal
    }
    public enum SwitchMode {
        Silent,
        PickSilent,
        InvSwitch
    }

    public enum MineType {
        Cev,
        TrapCev,
        SurroundCev,
        SurroundMiner,
        AutoCity,
        AntiBurrow,
        Manual
    }

    private record Target(BlockPos pos, BlockPos crystalPos, MineType type, double priority, boolean civ, boolean manual) {
    }
}

