/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package tcy.addon.atrihack.modules.conbot;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.SortPriority;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.util.math.BlockPos;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.modules.render.MineESP;

public class AutoWebPlus extends ArkModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> range = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("The maximum distance to target players.")
        .defaultValue(4)
        .range(0, 7)
        .sliderMax(7)
        .build()
    );
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder()
        .name("Pause Eat")
        .description("Pauses when you are eating.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SortPriority> priority = sgGeneral.add(new EnumSetting.Builder<SortPriority>()
        .name("target-priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.LowestDistance)
        .build()
    );

    private final Setting<Boolean> doubles = sgGeneral.add(new BoolSetting.Builder()
        .name("doubles")
        .description("Places webs in the target's upper hitbox as well as the lower hitbox.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the webs when placing.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> onlyground = sgGeneral.add(new BoolSetting.Builder()
        .name("OnlyGround")
        .description("Rotates towards the webs when placing.")
        .defaultValue(true)
        .build()
    );
    private PlayerEntity target = null;
    BlockPos breakpos = null;
    String name = "";
    public AutoWebPlus() {
        super(ATRIHack.atricombot, "smart-web+", "Automatically places webs on other players.");
    }
    private boolean shouldPause() {
        return !pauseEat.get() || !mc.player.isUsingItem();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (TargetUtils.isBadTarget(target, range.get())) {
            target = TargetUtils.getPlayerTarget(range.get(), priority.get());
            if (TargetUtils.isBadTarget(target, range.get())) return;
        }
        if(onlyground.get()){
            if(target.isOnGround() && shouldPause() && breakpos!=target.getBlockPos()) {
                BlockUtils.place(target.getBlockPos(), InvUtils.findInHotbar(Items.COBWEB), rotate.get(), 25, false);

                if (doubles.get() && mc.world.getBlockState(target.getBlockPos()).getBlock()!= Blocks.AIR) {
                    BlockUtils.place(target.getBlockPos().add(0, 1, 0), InvUtils.findInHotbar(Items.COBWEB), rotate.get(), 25, false);
                }
            }
        }else {
            shouldPause();
            if(breakpos!=target.getBlockPos())return;
            BlockUtils.place(target.getBlockPos(), InvUtils.findInHotbar(Items.COBWEB), rotate.get(), 25, false);

            if (doubles.get() && mc.world.getBlockState(target.getBlockPos()).getBlock()!= Blocks.AIR) {
                BlockUtils.place(target.getBlockPos().add(0, 1, 0), InvUtils.findInHotbar(Items.COBWEB), rotate.get(), 25, false);
            }
        }
    }
    @EventHandler
    private void onReceive(PacketEvent.Receive event) {
        if (event.packet instanceof BlockBreakingProgressS2CPacket packet) {
            breakpos = packet.getPos();
            name = String.valueOf(packet.getEntityId());
        }
    }
}
