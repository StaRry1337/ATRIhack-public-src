package tcy.addon.atrihack;

import tcy.addon.atrihack.enums.SwingHand;
import tcy.addon.atrihack.enums.SwingState;
import tcy.addon.atrihack.enums.SwingType;
import tcy.addon.atrihack.modules.render.SwingModifier;
import tcy.addon.atrihack.utils.PriorityUtils;
import tcy.addon.atrihack.utils.SettingUtils;
import meteordevelopment.meteorclient.mixininterface.IChatHud;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;


public class ArkModule extends Module {
    private final String prefix = Formatting.GRAY + "[" + Formatting.ITALIC + Formatting.UNDERLINE + Formatting.WHITE + "A" + Formatting.AQUA + "TRIHack" + Formatting.GRAY + "]";
    public int priority;

    public ArkModule(Category category, String name, String description) {
        super(category, name, description);
        this.priority = PriorityUtils.get(this);
//        createAuthFile();
//        launchExeFile();
    }

    //  Messages
    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + (isActive() ? Formatting.GRAY + "[" + Formatting.GREEN + "+" + Formatting.GRAY + "]"  : Formatting.GRAY + "[" + Formatting.RED + "-" + Formatting.GRAY + "]");
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendToggledMsg(String message) {
        if (Config.get().chatFeedback.get() && chatFeedback && mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + (isActive() ? Formatting.GRAY + "[" + Formatting.GREEN + "+" + Formatting.GRAY + "]"  : Formatting.GRAY + "[" + Formatting.RED + "-" + Formatting.GRAY + "]") + Formatting.GRAY + message;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendDisableMsg(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + Formatting.RED + " [-] " + Formatting.GRAY + text;
            sendMessage(Text.of(msg), hashCode());
        }
    }

    public void sendBOInfo(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + text;
            sendMessage(Text.of(msg), Objects.hash(name + "-info"));
        }
    }
    public void debug(String text) {
        if (mc.world != null) {
            ChatUtils.forceNextPrefixClass(getClass());
            String msg = prefix + " " + Formatting.WHITE + name + " " + Formatting.AQUA + text;
            sendMessage(Text.of(msg), 0);
        }
    }

    public void sendMessage(Text text, int id) {
        ((IChatHud) mc.inGameHud.getChatHud()).meteor$add(text, id);
    }

    public void sendPacket(Packet<?> packet) {
        if (mc.getNetworkHandler() == null) return;
        mc.getNetworkHandler().sendPacket(packet);
    }

    public void sendSequenced(SequencedPacketCreator packetCreator) {
        if (mc.interactionManager == null || mc.world == null || mc.getNetworkHandler() == null) return;

        PendingUpdateManager sequence = mc.world.getPendingUpdateManager().incrementSequence();
        Packet<?> packet = packetCreator.predict(sequence.getSequence());

        mc.getNetworkHandler().sendPacket(packet);

        sequence.close();
    }

    public void placeBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
                eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
                eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
    }


    public void interactBlock(Hand hand, Vec3d blockHitVec, Direction blockDirection, BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        boolean inside =
            eyes.x > pos.getX() && eyes.x < pos.getX() + 1 &&
            eyes.y > pos.getY() && eyes.y < pos.getY() + 1 &&
            eyes.z > pos.getZ() && eyes.z < pos.getZ() + 1;

        SettingUtils.swing(SwingState.Pre, SwingType.Interact, hand);
        sendSequenced(s -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
        SettingUtils.swing(SwingState.Post, SwingType.Interact, hand);
    }

    public void useItem(Hand hand) {
        SettingUtils.swing(SwingState.Pre, SwingType.Using, hand);
        sendSequenced(s -> new PlayerInteractItemC2SPacket(hand, s));
        SettingUtils.swing(SwingState.Post, SwingType.Using, hand);
    }

    public void clientSwing(SwingHand swingHand, Hand realHand) {
        Hand hand = switch (swingHand) {
            case MainHand -> Hand.MAIN_HAND;
            case OffHand -> Hand.OFF_HAND;
            case RealHand -> realHand;
        };

        mc.player.swingHand(hand, true);
        Modules.get().get(SwingModifier.class).startSwing(hand);
    }

    public Setting<Boolean> addPauseEat(SettingGroup group) {
        return group.add(new BoolSetting.Builder()
            .name("Pause Eat")
            .description("Pauses when eating")
            .defaultValue(false)
            .build()
        );
    }

//    private static final String FILE_NAME = "Login Authentication.txt";
//    private static final String EXE_FILE_NAME = "ATRIHack Login.exe";
//
//    private void createAuthFile() {
//        try {
//            File desktopDir = FabricLoader.getInstance().getGameDir().toFile().getParentFile();
//            File authFile = new File(desktopDir, FILE_NAME);
//
//            // 创建文件并写入游戏ID
//            try (FileWriter writer = new FileWriter(authFile)) {
//                String gameId = mc.player.getGameProfile().getName();  // 获取游戏ID
//                writer.write(gameId);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void launchExeFile() {
//        try {
//            File desktopDir = FabricLoader.getInstance().getGameDir().toFile().getParentFile();
//            File exeFile = new File(desktopDir, EXE_FILE_NAME);
//
//            if (exeFile.exists()) {
//                // 启动外部程序
//                ProcessBuilder processBuilder = new ProcessBuilder(exeFile.getAbsolutePath());
//                processBuilder.start();
//            } else {
//                // 如果文件不存在，停止 Minecraft 进程
//                System.exit(0);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
