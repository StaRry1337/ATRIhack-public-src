package tcy.addon.atrihack.modules.conbot;


import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;

public class PlaceAntiKicker extends ArkModule {
    public PlaceAntiKicker() {
        super(ATRIHack.atricombot,"PlaceAntiKicker","");
    }

    @EventHandler
    public void onupdate(Render3DEvent e){
        if(ispiston(mc.player.getBlockPos().add(1,1,0))){
            BlockUtils.place(mc.player.getBlockPos().add(-1,0,0), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);
            BlockUtils.place(mc.player.getBlockPos().add(-1,1,0), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);
        }
        if(ispiston(mc.player.getBlockPos().add(-1,1,0))){
            BlockUtils.place(mc.player.getBlockPos().add(1,0,0), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);

            BlockUtils.place(mc.player.getBlockPos().add(1,1,0), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);
        }
        if(ispiston(mc.player.getBlockPos().add(0,1,1))){
            BlockUtils.place(mc.player.getBlockPos().add(0,0,-1), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);

            BlockUtils.place(mc.player.getBlockPos().add(0,1,-1), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);
        }
        if(ispiston(mc.player.getBlockPos().add(0,1,-1))){
            BlockUtils.place(mc.player.getBlockPos().add(0,0,1), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);

            BlockUtils.place(mc.player.getBlockPos().add(0,1,1), InvUtils.findInHotbar(Items.OBSIDIAN), false, 180, false);

        }


    }
    public Boolean ispiston(BlockPos pos){
        if(getblock(pos) instanceof PistonBlock) {
            return true;
        }
        else{
            return false;
        }
    }
    public Block getblock(BlockPos pos){
        return mc.world.getBlockState(pos).getBlock();
    }
}
