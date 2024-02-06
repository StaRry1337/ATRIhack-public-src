package tcy.addon.atrihack.modules.conbot;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.utils.timers.TimerUtils;

public class AntiPiston extends ArkModule {
    public AntiPiston(){
        super(ATRIHack.atricombot,"AntiPiston","ee");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("How many offset packets to send.")
        .defaultValue(10)
        .min(0)
        .sliderRange(0, 250)
        .build()
    );
    private final TimerUtils timer = new TimerUtils();
    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if(mc.player.isOnGround()){
            if(mc.world.getBlockState(new BlockPos(mc.player.getBlockX()-1,  mc.player.getBlockY(), mc.player.getBlockZ())).getBlock()!= Blocks.OBSIDIAN &&
                mc.world.getBlockState(new BlockPos(mc.player.getBlockX()+1,  mc.player.getBlockY(), mc.player.getBlockZ())).getBlock()!= Blocks.OBSIDIAN &&
                mc.world.getBlockState(new BlockPos(mc.player.getBlockX(),  mc.player.getBlockY(), mc.player.getBlockZ()-1)).getBlock()!= Blocks.OBSIDIAN &&
                mc.world.getBlockState(new BlockPos(mc.player.getBlockX()-1,  mc.player.getBlockY(), mc.player.getBlockZ()+1)).getBlock()!= Blocks.OBSIDIAN){

                if(mc.world.getBlockState(new BlockPos(mc.player.getBlockX()+1,  mc.player.getBlockY()+1, mc.player.getBlockZ())).getBlock()!= Blocks.PISTON){
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX()-1,mc.player.getBlockY()+1,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX()-1,mc.player.getBlockY()+1,mc.player.getBlockZ()));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX()-1,mc.player.getBlockY()+2,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX()-1,mc.player.getBlockY()+2,mc.player.getBlockZ()));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+2,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+2,mc.player.getBlockZ()));
                }

                if(mc.world.getBlockState(new BlockPos(mc.player.getBlockX()-1,  mc.player.getBlockY()+1, mc.player.getBlockZ())).getBlock()!= Blocks.PISTON){
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX()+1,mc.player.getBlockY()+1,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX()+1,mc.player.getBlockY()+1,mc.player.getBlockZ()));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX()+1,mc.player.getBlockY()+2,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX()+1,mc.player.getBlockY()+2,mc.player.getBlockZ()));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+2,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+2,mc.player.getBlockZ()));
                }

                if(mc.world.getBlockState(new BlockPos(mc.player.getBlockX(),  mc.player.getBlockY()+1, mc.player.getBlockZ()+1)).getBlock()!= Blocks.PISTON){
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+1,mc.player.getBlockZ()-1), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+1,mc.player.getBlockZ()-1));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+2,mc.player.getBlockZ()-1), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+2,mc.player.getBlockZ()-1));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+2,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+2,mc.player.getBlockZ()));
                }

                if(mc.world.getBlockState(new BlockPos(mc.player.getBlockX(),  mc.player.getBlockY()+1, mc.player.getBlockZ()-1)).getBlock()!= Blocks.PISTON){
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+1,mc.player.getBlockZ()+1), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+1,mc.player.getBlockZ()+1));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+2,mc.player.getBlockZ()+1), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+1,mc.player.getBlockZ()+1));
                    this.timer.passedMs(this.delay.getDefaultValue());
                    placeBlock(Hand.MAIN_HAND, new Vec3d(mc.player.getX(),mc.player.getBlockY()+2,mc.player.getBlockZ()), Direction.UP, new BlockPos(mc.player.getBlockX(),mc.player.getBlockY()+1,mc.player.getBlockZ()));
                }
            }
        }
        else{
            return;
        }
    }
}
