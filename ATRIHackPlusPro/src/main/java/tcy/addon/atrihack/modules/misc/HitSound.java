package tcy.addon.atrihack.modules.misc;

import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.sound.SoundCategory;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.events.EventAttack;
import tcy.addon.atrihack.utils.ThSoundPack;
import tcy.addon.atrihack.utils.sound.MathUtility;

public class HitSound extends Module {
    public HitSound(){
        super(ATRIHack.atrimisc,"HitShound","Sound");
    }
    private final SettingGroup sgsound = settings.createGroup("Sound");

    private final Setting<Mode> mode = sgsound.add(new EnumSetting.Builder<Mode>()
        .name("Render Mode")
        .description("What should the render look like.")
        .defaultValue(Mode.MOAN)
        .build()
    );
    private final Setting<Double> volume = sgsound.add(new DoubleSetting.Builder()
        .name("volume")
        .description("")
        .defaultValue(1f)
        .min(0.1f)
        .sliderRange(0.1f, 10f)
        .build()
    );
    private final Setting<Double> pitch = sgsound.add(new DoubleSetting.Builder()
        .name("pitch")
        .description("")
        .defaultValue(1f)
        .min(0.1f)
        .sliderRange(0.1f, 10f)
        .build()
    );
    @EventHandler
    public void onAttack(EventAttack event) {
        if (!(event.getEntity() instanceof EndCrystalEntity)) {
            if(mode.getDefaultValue() == Mode.UWU){
                mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.UWU_SOUNDEVENT, SoundCategory.BLOCKS);
            }
            if(mode.getDefaultValue() == Mode.SKEET){
                mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.SKEET_SOUNDEVENT, SoundCategory.BLOCKS);
            }
            if(mode.getDefaultValue() == Mode.KEYBOARD){
                mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.KEYPRESS_SOUNDEVENT, SoundCategory.BLOCKS);
            }
            if(mode.getDefaultValue() == Mode.MOAN){
                int i = (int) (MathUtility.random(0,5));
                if(i == 0){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN1_SOUNDEVENT, SoundCategory.BLOCKS);
                }
                if(i == 1){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN2_SOUNDEVENT, SoundCategory.BLOCKS);
                }
                if(i == 2){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN3_SOUNDEVENT, SoundCategory.BLOCKS);
                }
                if(i == 3){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN4_SOUNDEVENT, SoundCategory.BLOCKS);
                }
                if(i == 5){
                    mc.world.playSound(mc.player, mc.player.getBlockPos(), ThSoundPack.MOAN5_SOUNDEVENT, SoundCategory.BLOCKS);
                }
            }
        }
    }

    public enum Mode {
        UWU, MOAN, SKEET, KEYBOARD
    }
}
