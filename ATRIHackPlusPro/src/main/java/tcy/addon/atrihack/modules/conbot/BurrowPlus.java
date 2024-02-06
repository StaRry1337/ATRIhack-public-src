package tcy.addon.atrihack.modules.conbot;


import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.enums.RotationType;
import tcy.addon.atrihack.enums.SwingHand;
import tcy.addon.atrihack.managers.Managers;
import tcy.addon.atrihack.utils.SettingUtils;

import java.util.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BurrowPlus extends ArkModule {
    public BurrowPlus() {
        super(ATRIHack.atricombot, "BurrowTest", "Let you clip into obsidian/enderchest.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRubberband = settings.createGroup("Rubberband");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgText = settings.createGroup("TextRender");

    //--------------------General--------------------//
    private final Setting<LagBackMode> lagBackMode = sgGeneral.add(new EnumSetting.Builder<LagBackMode>()
        .name("LagBack Mode")
        .description("")
        .defaultValue(LagBackMode.Seija)
        .build()
    );
    private final Setting<Boolean> breakcrystal = sgGeneral.add(new BoolSetting.Builder()
        .name("Break Crystal")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> headFill = sgGeneral.add(new BoolSetting.Builder()
        .name("Head Fill")
        .description("")
        .defaultValue(false)
        .build()
    );
    private final Setting<HeadBlock> headBlock = sgGeneral.add(new EnumSetting.Builder<HeadBlock>()
        .name("Head Block")
        .description("")
        .defaultValue(HeadBlock.Obsidian)
        .visible(headFill::get)
        .build()
    );
    private final Setting<FeetBlock> feetBlock = sgGeneral.add(new EnumSetting.Builder<FeetBlock>()
        .name("Feet Block")
        .description("")
        .defaultValue(FeetBlock.Obsidian)
        .build()
    );
    private final Setting<Integer> tryCount = sgGeneral.add(new IntSetting.Builder()
        .name("Try Count")
        .description("")
        .defaultValue(0)
        .range(0, 20)
        .sliderRange(0, 20)
        .build()
    );

    //--------------------Rubberband--------------------//
    private final Setting<Double> rubberbandOffset = sgRubberband.add(new DoubleSetting.Builder()
        .name("Rubberband Offset")
        .description("Y offset of rubberband packet.")
        .defaultValue(9)
        .sliderRange(-10, 10)
        .visible(() -> lagBackMode.get().equals(LagBackMode.Up))
        .build()
    );
    private final Setting<Integer> rubberbandPackets = sgRubberband.add(new IntSetting.Builder()
        .name("Rubberband Packets")
        .description("How many offset packets to send.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .visible(() -> lagBackMode.get().equals(LagBackMode.Up))
        .build()
    );

    //--------------------Render--------------------//
    private final Setting<Boolean> swing = sgRender.add(new BoolSetting.Builder()
        .name("Swing")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be use on swing.")
        .defaultValue(SwingHand.RealHand)
        .build()
    );
    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("Render")
        .description("")
        .defaultValue(true)
        .build()
    );
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of the boxes should be rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Lines) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );
    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 50))
        .visible(() -> render.get() && (shapeMode.get().equals(ShapeMode.Sides) || shapeMode.get().equals(ShapeMode.Both)))
        .build()
    );

    //--------------------Text--------------------//
    private final Setting<Double> scale = sgText.add(new DoubleSetting.Builder()
        .name("Scale")
        .description("Scale to render at.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 10)
        .build()
    );
    private final Setting<Double> textx = sgText.add(new DoubleSetting.Builder()
        .name("X")
        .description("X to render at.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 255)
        .build()
    );
    private final Setting<Double> texty = sgText.add(new DoubleSetting.Builder()
        .name("Y")
        .description("Y to render at.")
        .defaultValue(1)
        .min(0)
        .sliderRange(0, 255)
        .build()
    );
    private final Setting<SettingColor> textColor = sgText.add(new ColorSetting.Builder()
        .name("Text Color")
        .description(ATRIHack.COLOR)
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );
    private final Setting<Boolean> shadow = sgText.add(new BoolSetting.Builder()
        .name("Text Shadow")
        .description("Should the text have a shadow.")
        .defaultValue(true)
        .build()
    );

    private double y = 0;
    private double velocity = 0.42;
    private int count;
    private final List<Render> renderBlocks = new ArrayList<>();

    @Override
    public void onActivate() {
        this.count = 0;
    }
    private Entity crystal;
//    @EventHandler(priority = EventPriority.HIGHEST)
//    private void onTick(TickEvent.Pre event) {
//        crystal = this.getBlocking();
//        if (crystal == null){return;}
//        sendPacket(PlayerInteractEntityC2SPacket.attack(crystal,mc.player.isSneaking()));
//        toggle();
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTick(TickEvent.Pre event) {
        crystal = this.getBlocking();
        if (crystal == null){return;}
        if(breakcrystal.get()==false){return;}
        sendPacket(PlayerInteractEntityC2SPacket.attack(crystal,mc.player.isSneaking()));

    }

    private Entity getBlocking() {
        Entity crystal = null;
        double lowest = 1000;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity)) continue;
            if (mc.player.distanceTo(entity) > 5) continue;
            if (!SettingUtils.inAttackRange(entity.getBoundingBox())) continue;
            crystal = entity;
        }
        return crystal;
    }

    @EventHandler
    public void onTick(TickEvent.Post event) {
        if (!mc.player.isOnGround()) {
            return;
        }

        int blockSlot = InvUtils.findInHotbar(Items.OBSIDIAN).slot() != -1 ? InvUtils.findInHotbar(Items.OBSIDIAN).slot() : InvUtils.findInHotbar(Items.ENDER_CHEST).slot();
        if (blockSlot == -1) {
            toggle();
            sendDisableMsg("Obsidian/Ender Chest ?");
            return;
        }
        final BlockPos burBlock = this.getFillBlock();
        if (burBlock == null) {
            toggle();
            return;
        }

        if(mc.world.getBlockState(BlockPos.fromLong(mc.player.getBlockY()-1)).getBlock() == Blocks.AIR){
            BlockUtils.place(mc.player.getBlockPos().add(0,-1,0), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);
        }
        final boolean headFillMode = burBlock.getY() >= mc.player.getY() + 0.4;
        final List<Vec3d> fakeJumpOffsets = this.getFakeJumpOffset(burBlock, headFillMode);
        if (fakeJumpOffsets.size() != 4) {
            toggle();
            return;
        }

        if (lagBackMode.get().equals(LagBackMode.Up)) {
            while (y < 1.1) {
                y += velocity;
                velocity = (velocity - 0.08) * 0.98;

                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y, mc.player.getZ(), false));
            }
        } else {
            updateFakeJump(fakeJumpOffsets);
        }

        updatePlace(burBlock, headFillMode);

        updateLagBlock();

        ++count;
        if (this.count >= tryCount.get()) toggle();
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!render.get()) return;

        renderBlocks.removeIf(r -> System.currentTimeMillis() - r.time > 1000);

        renderBlocks.forEach(r -> {
            double progress = 1 - Math.min(System.currentTimeMillis() - r.time, 500) / 500d;

            event.renderer.box(r.pos, new Color(sideColor.get().r, sideColor.get().g, sideColor.get().b, (int) Math.round(sideColor.get().a * progress)), new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * progress)), shapeMode.get(), 0);
        });
    }

    @EventHandler
    private void onrender2d(HudRenderer renderer){
        if (mc.player == null || mc.world == null) {return;}
        if(mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos())).getBlock()== Blocks.AIR)return;

        String text = "You are in burrow :" + mc.world.getBlockState(BlockPos.ofFloored(mc.player.getPos())).getBlock().toString() + "[ " + mc.player.getPos().toString() + " ]";
        renderer.text(text, textx.get(), texty.get(), textColor.get(), shadow.get(), scale.get());

    }
    public static Color injectAlpha(final Color color, final int alpha) {
        int alph = MathHelper.clamp(alpha, 0, 255);
        return new Color(color.r, color.g, color.b, alph);
    }

    private List<Vec3d> getFakeJumpOffset(BlockPos burBlock, boolean headFillMode) {
        final List<Vec3d> offsets = new LinkedList<>();
        if (headFillMode) {
            if (fakeBBoxCheckFeet(mc.player, new Vec3d(0.0, 2.5, 0.0))) {
                final Vec3d offVec = get2BurFjPos(burBlock);
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.42132, mc.player.getY() + 0.41999998688698, mc.player.getZ() + offVec.z * 0.42132));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.95, mc.player.getY() + 0.7500019, mc.player.getZ() + offVec.z * 0.95));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.03, mc.player.getY() + 0.9999962, mc.player.getZ() + offVec.z * 1.03));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.0933, mc.player.getY() + 1.17000380178814, mc.player.getZ() + offVec.z * 1.0933));
            } else {
                final Vec3d offVec = get2BurFjPos(burBlock);
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.42132, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 0.42132));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.95, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 0.95));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.03, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 1.03));
                offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.0933, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 1.0933));
            }
        } else if (fakeBBoxCheckFeet(mc.player, new Vec3d(0.0, 2.5, 0.0))) {
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 0.41999998688698, mc.player.getZ()));
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 0.7500019, mc.player.getZ()));
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 0.9999962, mc.player.getZ()));
            offsets.add(new Vec3d(mc.player.getX(), mc.player.getY() + 1.17000380178814, mc.player.getZ()));
        } else {
            final Vec3d offVec = get2BurFjPos(burBlock);
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.42132, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 0.42132));
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 0.95, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 0.95));
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.03, mc.player.getY() + 0.200000047683716, mc.player.getZ() + offVec.z * 1.03));
            offsets.add(new Vec3d(mc.player.getX() + offVec.x * 1.0933, mc.player.getY() + 0.12160004615784, mc.player.getZ() + offVec.z * 1.0933));
        }
        return offsets;
    }

    private boolean vec3dIsAir(Vec3d vec3d) {
        return mc.world.getBlockState(vec3toBlockPos(vec3d, true)).getBlock().equals(Blocks.AIR);
    }

    private BlockPos vec3toBlockPos(final Vec3d vec3d, final boolean Yfloor) {
        if (Yfloor) {
            return BlockPos.ofFloored(Math.floor(vec3d.x), Math.floor(vec3d.y), Math.floor(vec3d.z));
        }
        return BlockPos.ofFloored(Math.floor(vec3d.x), (double)Math.round(vec3d.y), Math.floor(vec3d.z));
    }

    private boolean fakeBBoxCheckFeet(final PlayerEntity player, final Vec3d offset) {
        final Vec3d futurePos = player.getPos().add(offset);
        return vec3dIsAir(futurePos.add(0.3, 0.0, 0.3)) && vec3dIsAir(futurePos.add(-0.3, 0.0, 0.3)) && vec3dIsAir(futurePos.add(0.3, 0.0, -0.3)) && vec3dIsAir(futurePos.add(-0.3, 0.0, 0.3));
    }

    private void updateFakeJump(final List<Vec3d> offsets) {
        if (offsets == null) {
            return;
        }
        offsets.stream().forEach(vec -> {
            if (vec != null && !vec.equals(Vec3d.ZERO)) {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(vec.x, vec.y, vec.z, false));
            }
        });
    }

    protected BlockPos getFillBlock() {
        final LinkedHashSet<BlockPos> feetBlock = getFeetBlock(0);
        final List<BlockPos> collect = feetBlock.stream()
                .filter(pos -> mc.world.isAir(pos))
                .filter(pos -> getHelpBlockFac(pos, SettingUtils.shouldRotate(RotationType.BlockPlace), true, null).size() != 0)
                .limit(1L)
                .toList();

        if (collect.isEmpty()) {
            return null;
        }

        return collect.get(0);
    }

    private List<Direction> getHelpBlockFac(final BlockPos pos, final boolean strictFac, final boolean noStrUpPlace, final List<Direction> ignoreFac) {
        final List<Direction> list = canTorchFac(pos);
        final List<Direction> canUseFac = new ArrayList<>();
        final Vec3d posVec = pos.toCenterPos();
        for (final Direction facing : Direction.values()) {
            if (!strictFac || !list.contains(facing)) {
                final Vec3d clickVec = posVec.add(new Vec3d(facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ()).multiply(0.5));
                if (SettingUtils.inPlaceRange(BlockPos.ofFloored(clickVec)) && (!mc.world.isAir(pos.offset(facing)) || (ignoreFac != null && ignoreFac.contains(facing)))) {
                    canUseFac.add(facing);
                }
            }
        }
        if (!canUseFac.contains(Direction.DOWN) && noStrUpPlace && mc.player.getY() - pos.getY() >= -2.0 && mc.player.getY() - pos.getY() <= 2.0 && (!mc.world.isAir(pos.offset(Direction.DOWN)) || (ignoreFac != null && ignoreFac.contains(Direction.DOWN)))) {
            canUseFac.add(Direction.DOWN);
        }
        return canUseFac;
    }

    private List<Direction> canTorchFac(final BlockPos pos) {
        final List<Direction> list = new ArrayList<>();
        if (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) < pos.getY()) {
            list.add(Direction.DOWN);
        }
        if (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()) > pos.getY() + 1) {
            list.add(Direction.UP);
        }
        if (mc.player.getZ() < pos.getX()) {
            list.add(Direction.WEST);
        }
        if (mc.player.getX() > pos.getX() + 1) {
            list.add(Direction.EAST);
        }
        if (mc.player.getZ() < pos.getZ()) {
            list.add(Direction.NORTH);
        }
        if (mc.player.getZ() > pos.getZ() + 1) {
            list.add(Direction.SOUTH);
        }
        return list;
    }

    public LinkedHashSet<BlockPos> getFeetBlock(final int yOff) {
        final LinkedHashSet<BlockPos> set = new LinkedHashSet<>();
        set.add(mc.player.getBlockPos().offset(Direction.UP, yOff));
        set.add(vec3toBlockPos(mc.player.getPos().add(0.3, yOff, 0.3), false));
        set.add(vec3toBlockPos(mc.player.getPos().add(-0.3, yOff, 0.3), false));
        set.add(vec3toBlockPos(mc.player.getPos().add(0.3, yOff, -0.3), false));
        set.add(vec3toBlockPos(mc.player.getPos().add(-0.3, yOff, -0.3), false));
        if (headFill.get() && yOff == 0) {
            set.addAll(getFeetBlock(1));
        }
        return set;
    }

    public Vec3d get2BurFjPos(final BlockPos burBlockPos) {
        final Vec3d v = new Vec3d(burBlockPos.getX(), burBlockPos.getY(), burBlockPos.getZ()).add(0.5, 0.5, 0.5);
        final BlockPos pPos = mc.player.getBlockPos();
        final Vec3d s = mc.player.getPos().subtract(v);
        Vec3d off = new Vec3d(0.0, 0.0, 0.0);
        if (Math.abs(s.x) >= Math.abs(s.z) && Math.abs(s.x) > 0.2) {
            if (s.x > 0.0) {
                off = new Vec3d(0.8 - s.x, 0.0, 0.0);
            }
            else {
                off = new Vec3d(-0.8 - s.x, 0.0, 0.0);
            }
        } else if (Math.abs(s.z) >= Math.abs(s.x) && Math.abs(s.z) > 0.2) {
            if (s.z > 0.0) {
                off = new Vec3d(0.0, 0.0, 0.8 - s.z);
            }
            else {
                off = new Vec3d(0.0, 0.0, -0.8 - s.z);
            }
        } else if (burBlockPos.equals(pPos)) {
            final List<Direction> facList = new ArrayList<>();
            for (final Direction f3 : Direction.values()) {
                if (f3 != Direction.UP) {
                    if (f3 != Direction.DOWN) {
                        if (mc.world.isAir(pPos.offset(f3)) && mc.world.isAir(pPos.offset(f3).offset(Direction.UP))) {
                            facList.add(f3);
                        }
                    }
                }
            }
            final Vec3d vec3d = Vec3d.ZERO;
            final Vec3d[] offVec1 = new Vec3d[1];
            final Vec3d[] offVec2 = new Vec3d[1];
            facList.sort((f1, f2) -> {
                offVec1[0] = vec3d.add(new Vec3d(f1.getOffsetX(), f1.getOffsetY(), f1.getOffsetZ()).multiply(0.5));
                offVec2[0] = vec3d.add(new Vec3d(f2.getOffsetX(), f2.getOffsetY(), f2.getOffsetZ()).multiply(0.5));
                return (int)(Math.sqrt(mc.player.squaredDistanceTo(offVec1[0].x, mc.player.getY(), offVec1[0].z)) - Math.sqrt(mc.player.squaredDistanceTo(offVec2[0].x, mc.player.getY(), offVec2[0].z)));
            });
            if (facList.size() > 0) {
                off = new Vec3d(facList.get(0).getOffsetX(), facList.get(0).getOffsetY(), facList.get(0).getOffsetZ());
            }
        }
        return off;
    }

    public final void updateLagBlock() {
        switch (lagBackMode.get()) {
            case Up -> {
                for (int i = 0; i < rubberbandPackets.get(); i++) {
                    sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y + rubberbandOffset.get(), mc.player.getZ(), false));
                }
            }
            case Troll -> {
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 3.3400880035762786, mc.player.getZ(), false));
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 1.0, mc.player.getZ(), false));
            }
            case Seija -> {
                if (mc.player.getY() >= 3.0) {
                    for (int i = -10; i < 10; ++i) {
                        if (i == -1) {
                            i = 4;
                        }
                        if (mc.world.getBlockState(mc.player.getBlockPos().add(0, i, 0)).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(mc.player.getBlockPos().add(0, i + 1, 0)).getBlock().equals(Blocks.AIR)) {
                            final BlockPos pos = mc.player.getBlockPos().add(0, i, 0);
                            sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(pos.getX() + 0.3, pos.getY(), pos.getZ() + 0.3, false));
                            return;
                        }
                    }
                }
                sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 5.0, mc.player.getZ(), false));
            }
            case Old -> sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 7.0, mc.player.getZ(), false));
        }
    }

    private void updatePlace(BlockPos pos, final boolean headFill) {
        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.start(pos, priority, RotationType.BlockPlace, Objects.hash(name + "placing"));

        int oldSlot = mc.player.getInventory().selectedSlot;

        InvUtils.swap(getBlockSlotFeet(feetBlock.get()), false);

        if (headFill) InvUtils.swap(getBlockSlotHead(headBlock.get()), false);

        //placeBlock(Hand.MAIN_HAND, data.pos().toCenterPos(), data.dir(), data.pos());

        // TODO: Shit code.
        placeBlock(mc.player.getPos());
        placeBlock(new Vec3d(mc.player.getX() - 0.3, mc.player.getY(), mc.player.getZ()));
        placeBlock(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() - 0.3));
        placeBlock(new Vec3d(mc.player.getX() - 0.3, mc.player.getY(), mc.player.getZ()));
        placeBlock(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() - 0.3));
        placeBlock(new Vec3d(mc.player.getX() - 0.3, mc.player.getY(), mc.player.getZ()));
        placeBlock(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() + 0.3));
        placeBlock(new Vec3d(mc.player.getX() + 0.3, mc.player.getY(), mc.player.getZ()));
        placeBlock(new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ() + 0.3));
        placeBlock(new Vec3d(mc.player.getX() + 0.3, mc.player.getY(), mc.player.getZ() + 0.3));
        placeBlock(new Vec3d(mc.player.getX() - 0.3, mc.player.getY(), mc.player.getZ() - 0.3));

        if (swing.get()) clientSwing(placeHand.get(), Hand.MAIN_HAND);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace))
            Managers.ROTATION.end(Objects.hash(name + "placing"));

        mc.player.getInventory().selectedSlot = oldSlot;
    }

    private void placeBlock(Vec3d vec3d) {
        BlockPos blockPos = BlockPos.ofFloored(vec3d);

        try {
            BlockUtils.place(blockPos, InvUtils.findInHotbar(Items.OBSIDIAN), false, 50, false);
            renderBlocks.add(new Render(blockPos, System.currentTimeMillis()));
        } catch (Exception ignored) {
        }
    }

    public int getBlockSlotHead(final HeadBlock mode) {
        int slot = -1;
        switch (mode) {
            case EndChest -> slot = (InvUtils.findInHotbar(Items.ENDER_CHEST).slot() == -1) ? InvUtils.findInHotbar(Items.OBSIDIAN).slot() : InvUtils.findInHotbar(Items.ENDER_CHEST).slot();
            case Obsidian -> slot = (InvUtils.findInHotbar(Items.OBSIDIAN).slot() == -1) ? InvUtils.findInHotbar(Items.ENDER_CHEST).slot() : InvUtils.findInHotbar(Items.OBSIDIAN).slot();
        }
        return slot;
    }

    public int getBlockSlotFeet(final FeetBlock mode) {
        int slot = -1;
        switch (mode) {
            case EndChest -> slot = (InvUtils.findInHotbar(Items.ENDER_CHEST).slot() == -1) ? InvUtils.findInHotbar(Items.OBSIDIAN).slot() : InvUtils.findInHotbar(Items.ENDER_CHEST).slot();
            case Obsidian -> slot = (InvUtils.findInHotbar(Items.OBSIDIAN).slot() == -1) ? InvUtils.findInHotbar(Items.ENDER_CHEST).slot() : InvUtils.findInHotbar(Items.OBSIDIAN).slot();
        }
        return slot;
    }

    public enum FeetBlock {
        EndChest,
        Obsidian
    }

    public enum HeadBlock {
        EndChest,
        Obsidian
    }

    public enum LagBackMode {
        Up,
        Seija,
        Troll,
        Old
    }

    public record Render(BlockPos pos, long time) {
    }
}
