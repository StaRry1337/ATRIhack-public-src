package tcy.addon.atrihack.utils;

import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class EntityUtils {
    public static Entity deadEntity;
    public static boolean isWebbed(PlayerEntity targetEntity) {
        return doesBoxTouchBlock(targetEntity.getBoundingBox(), Blocks.COBWEB);
    }
    public static boolean isDeathPacket(PacketEvent.Receive event) {
        if (event.packet instanceof EntityStatusS2CPacket packet) {
            if (packet.getStatus() == 3) {
                deadEntity = packet.getEntity(mc.world);
                return deadEntity instanceof PlayerEntity;
            }
        }
        return false;
    }
    public static boolean doesBoxTouchBlock(Box box, Block block) {
        for (int x = (int) Math.floor(box.minX); x < Math.ceil(box.maxX); x++) {
            for (int y = (int) Math.floor(box.minY); y < Math.ceil(box.maxY); y++) {
                for (int z = (int) Math.floor(box.minZ); z < Math.ceil(box.maxZ); z++) {
                    if (mc.world.getBlockState(new BlockPos(x, y, z)).getBlock() == block) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    public static boolean isBurrowed(PlayerEntity targetEntity, BlastResistantType type) {
        BlockPos playerPos = roundBlockPos(new Vec3d(targetEntity.getX(), targetEntity.getY() + 0.4, targetEntity.getZ()));
        // Adding a 0.4 to the Y check since sometimes when the player moves around weirdly/ after chorusing they tend to clip into the block under them
        return isBlastResistant(playerPos, type);
    }



    public static boolean isBlastResistant(BlockPos pos, BlastResistantType type) {
        Block block = mc.world.getBlockState(pos).getBlock();
        switch (type) {
            case Any, Mineable -> {
                return block == Blocks.OBSIDIAN
                    || block == Blocks.CRYING_OBSIDIAN
                    || block instanceof AnvilBlock
                    || block == Blocks.NETHERITE_BLOCK
                    || block == Blocks.ENDER_CHEST
                    || block == Blocks.RESPAWN_ANCHOR
                    || block == Blocks.ANCIENT_DEBRIS
                    || block == Blocks.ENCHANTING_TABLE
                    || (block == Blocks.BEDROCK && type == BlastResistantType.Any)
                    || (block == Blocks.END_PORTAL_FRAME && type == BlastResistantType.Any);
            }
            case Unbreakable -> {
                return block == Blocks.BEDROCK
                    || block == Blocks.END_PORTAL_FRAME;
            }
            case NotAir -> {
                return block != Blocks.AIR;
            }
        }
        return false;
    }
    public static boolean isSurrounded(PlayerEntity player, BlastResistantType type) {
        BlockPos blockPos = player.getBlockPos();

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) continue;
            if (!isBlastResistant(blockPos, type)) return false;
        }

        return true;
    }
    public static BlockPos playerPos(PlayerEntity targetEntity) {
        return EntityUtils.roundBlockPos(targetEntity.getPos());
    }
    public static BlockPos roundBlockPos(Vec3d vec) {
        return new BlockPos((int) vec.x, (int) Math.round(vec.y), (int) vec.z);
    }

    public static List<BlockPos> getSurroundBlocks(PlayerEntity player) {
        if (player == null) return null;

        List<BlockPos> positions = new ArrayList<>();

        for (Direction direction : Direction.values()) {
            if (direction == Direction.UP || direction == Direction.DOWN) continue;
            BlockPos pos = playerPos(player).offset(direction);
            if (isBlastResistant(pos, BlastResistantType.Mineable)) { positions.add(pos); }
        }

        return positions;
    }
    public static BlockPos getCityBlock(PlayerEntity player) {
        List<BlockPos> posList = getSurroundBlocks(player);
        posList.sort(Comparator.comparingDouble(PlayerUtils::distanceFromEye));
        return posList.isEmpty() ? null : posList.get(0);
    }
    public static BlockPos getTargetBlock(PlayerEntity player) {
        BlockPos finalPos = null;

        List<BlockPos> positions = getSurroundBlocks(player);
        List<BlockPos> myPositions = getSurroundBlocks(mc.player);

        if (positions == null) return null;

        for (BlockPos pos : positions) {

            if (myPositions != null && !myPositions.isEmpty() && myPositions.contains(pos)) continue;

            if (finalPos == null) {
                finalPos = pos;
                continue;
            }

            if (mc.player.squaredDistanceTo(meteordevelopment.meteorclient.utils.Utils.vec3d(pos)) < mc.player.squaredDistanceTo(Utils.vec3d(finalPos))) {
                finalPos = pos;
            }
        }

        return finalPos;
    }
    public enum BlastResistantType {
        Any, // Any blast resistant block
        Unbreakable, // Can't be mined
        Mineable, // You can mine the block
        NotAir // Doesn't matter as long it's not air
    }
}
