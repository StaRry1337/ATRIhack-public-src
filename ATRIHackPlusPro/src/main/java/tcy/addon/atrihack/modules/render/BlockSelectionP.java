/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package tcy.addon.atrihack.modules.render;


import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IBox;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import tcy.addon.atrihack.ATRIHack;

public class BlockSelectionP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> advanced = sgGeneral.add(new BoolSetting.Builder()
            .name("advanced")
            .description("Shows a more advanced outline on different types of shape blocks.")
            .defaultValue(true)
            .build()
    );

    private final Setting<Boolean> oneSide = sgGeneral.add(new BoolSetting.Builder()
            .name("single-side")
            .description("Only renders the side you are looking at.")
            .defaultValue(false)
            .build()
    );
    private final Setting<Boolean> smooth = sgGeneral.add(new BoolSetting.Builder()
            .name("Smooth")
            .description("")
            .defaultValue(true)
            .build()
    );
    private final Setting<Integer> smoothness = sgGeneral.add(new IntSetting.Builder()
            .name("Smoothness")
            .description("How smoothly the render should move around.")
            .defaultValue(10)
            .min(0)
            .sliderMax(20)
            .visible(smooth::get)
            .build()
    );
    private final Setting<Integer> renderTime = sgGeneral.add(new IntSetting.Builder()
            .name("render time")
            .description("How long to render placements.")
            .defaultValue(10)
            .min(0)
            .sliderMax(20)
            .visible(smooth::get)
            .build()
    );
    private final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
            .name("shape-mode")
            .description("How the shapes are rendered.")
            .defaultValue(ShapeMode.Both)
            .build()
    );

    private final Setting<SettingColor> sideColor = sgGeneral.add(new ColorSetting.Builder()
            .name("side-color")
            .description("The side color.")
            .defaultValue(new SettingColor(255, 255, 255, 50))
            .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
            .name("line-color")
            .description("The line color.")
            .defaultValue(new SettingColor(255, 255, 255, 255))
            .build()
    );

    private final Setting<Boolean> hideInside = sgGeneral.add(new BoolSetting.Builder()
            .name("hide-when-inside-block")
            .description("Hide selection when inside target block.")
            .defaultValue(true)
            .build()
    );

    public BlockSelectionP() {
        super(ATRIHack.atrirender, "BlockSelection+", "Modifies how your block selection is rendered.");
    }
    private Box renderBoxOne, renderBoxTwo;


    @EventHandler
    private void onRender(Render3DEvent event) {
        if (mc.crosshairTarget == null || !(mc.crosshairTarget instanceof BlockHitResult result)) return;

        if (hideInside.get() && result.isInsideBlock()) return;

        BlockPos bp = result.getBlockPos();
        Direction side = result.getSide();

        BlockState state = mc.world.getBlockState(bp);
        VoxelShape shape = state.getOutlineShape(mc.world, bp);

        if (shape.isEmpty()) return;
        Box box = shape.getBoundingBox();
        if(!smooth.get()) {
            if (oneSide.get()) {
                if (side == Direction.UP || side == Direction.DOWN) {
                    event.renderer.sideHorizontal(bp.getX() + box.minX, bp.getY() + (side == Direction.DOWN ? box.minY : box.maxY), bp.getZ() + box.minZ, bp.getX() + box.maxX, bp.getZ() + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get());
                } else if (side == Direction.SOUTH || side == Direction.NORTH) {
                    double z = side == Direction.NORTH ? box.minZ : box.maxZ;
                    event.renderer.sideVertical(bp.getX() + box.minX, bp.getY() + box.minY, bp.getZ() + z, bp.getX() + box.maxX, bp.getY() + box.maxY, bp.getZ() + z, sideColor.get(), lineColor.get(), shapeMode.get());
                } else {
                    double x = side == Direction.WEST ? box.minX : box.maxX;
                    event.renderer.sideVertical(bp.getX() + x, bp.getY() + box.minY, bp.getZ() + box.minZ, bp.getX() + x, bp.getY() + box.maxY, bp.getZ() + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get());
                }
            } else {
                if (advanced.get()) {
                    if (shapeMode.get() == ShapeMode.Both || shapeMode.get() == ShapeMode.Lines) {
                        shape.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> {
                            event.renderer.line(bp.getX() + minX, bp.getY() + minY, bp.getZ() + minZ, bp.getX() + maxX, bp.getY() + maxY, bp.getZ() + maxZ, lineColor.get());
                        });
                    }

                    if (shapeMode.get() == ShapeMode.Both || shapeMode.get() == ShapeMode.Sides) {
                        for (Box b : shape.getBoundingBoxes()) {
                            render(event, bp, b);
                        }
                    }
                } else {
                    render(event, bp, box);
                }
            }
        }
        else{
            smoothrd(event,bp);
        }
    }

    private void render(Render3DEvent event, BlockPos bp, Box box) {
        event.renderer.box(bp.getX() + box.minX, bp.getY() + box.minY, bp.getZ() + box.minZ, bp.getX() + box.maxX, bp.getY() + box.maxY, bp.getZ() + box.maxZ, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
    private void smoothrd(Render3DEvent event, BlockPos renderPos) {
        if (renderTime.get() <= 0) return;

        if (renderBoxOne == null) renderBoxOne = new Box(renderPos);
        if (renderBoxTwo == null) {
            renderBoxTwo = new Box(renderPos);
        } else {
            ((IBox) renderBoxTwo).set(renderPos);
        }

        double offsetX = (renderBoxTwo.minX - renderBoxOne.minX) / smoothness.get();
        double offsetY = (renderBoxTwo.minY - renderBoxOne.minY) / smoothness.get();
        double offsetZ = (renderBoxTwo.minZ - renderBoxOne.minZ) / smoothness.get();

        ((IBox) renderBoxOne).set(
                renderBoxOne.minX + offsetX,
                renderBoxOne.minY + offsetY,
                renderBoxOne.minZ + offsetZ,
                renderBoxOne.maxX + offsetX,
                renderBoxOne.maxY + offsetY,
                renderBoxOne.maxZ + offsetZ
        );

        event.renderer.box(renderBoxOne, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
    }
}
