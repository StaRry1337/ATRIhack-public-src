package tcy.addon.atrihack.modules;


import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.managers.Managers;
import tcy.addon.atrihack.utils.others.Task;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.events.game.GameJoinedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.world.CardinalDirection;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockBreakingProgressS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

import static tcy.addon.atrihack.hud.ToastNotifications.addToast;
import static tcy.addon.atrihack.utils.entity.EntityInfo.getBlockPos;
import static tcy.addon.atrihack.utils.world.BlockInfo.isCombatBlock;

public class Notifications extends ArkModule {
    private final SettingGroup sgArmour = settings.createGroup("Breaks");
    private final SettingGroup sgPlayers = settings.createGroup("Players");
    private final SettingGroup sgSurround = settings.createGroup("Surround");

    private final SettingGroup sgNone = settings.createGroup("");
    private final SettingGroup sgRender =settings.getDefaultGroup();
    private final Setting<Mode> notifications = sgNone.add(new EnumSetting.Builder<Mode>().name("notifications").defaultValue(Mode.Toast).build());

    // Armor
    private final Setting<Boolean> armor = sgArmour.add(new BoolSetting.Builder().name("armor").description("Sends notifications while armor is low.").defaultValue(true).build());
    private final Setting<Integer> percentage = sgArmour.add(new IntSetting.Builder().name("percentage").description("Precentage of armor to trigger notifier.").defaultValue(40).sliderRange(1, 100) .visible(armor::get).build());
    private final Setting<String> message = sgArmour.add(new StringSetting.Builder().name("message").description("Messages for armor notify.").defaultValue("Your {armor}({value}%) is lower than {%}%!").build());

    // Players
    private final Setting<Boolean> totemNotif = sgPlayers.add(new BoolSetting.Builder().name("totem-pops").description("Sends notification for totem pops.").defaultValue(false).build());
    private final Setting<Boolean> deaths = sgPlayers.add(new BoolSetting.Builder().name("deaths").description("Sends notification for deaths.").defaultValue(false).build());

    // Surround
    private final Setting<Boolean> surroundBreak = sgSurround.add(new BoolSetting.Builder().name("surround-break").description("Notifies you while someone breaking your surround.").defaultValue(false).build());
    private final Setting<Boolean> surroundBreakrender = sgSurround.add(new BoolSetting.Builder().name("surround-Render").description(".").defaultValue(false).build());

    // Render
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("Line Color").description("Line color of rendered boxes").defaultValue(new SettingColor(255, 0, 0, 255)).build());
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder().name("Side Color").description("Side color of rendered boxes").defaultValue(new SettingColor(255, 0, 0, 50)).build());
    private final Setting<Double> fadeTime = sgRender.add(new DoubleSetting.Builder().name("Fade Time").description("How long the fading should take.").defaultValue(1).min(0).sliderRange(0, 10).build());
    private final Setting<Double> renderTime = sgRender.add(new DoubleSetting.Builder().name("Render Time").description("How long the box should remain in full alpha value.").defaultValue(0.3).min(0).sliderRange(0, 10).build());
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("Shape Mode").description("Which parts of render should be rendered.").defaultValue(ShapeMode.Both).build());

    public Notifications() {
        super(ATRIHack.ArkMode, "Notifications", "Sends messages in hud about different events.");
    }

    private BlockPos prevBreakPos;
    private final Task bootsTask = new Task();
    private final Task leggingsTask = new Task();
    private final Task chestplateTask = new Task();
    private final Task helmetTask = new Task();
    private double renderProgress = 0;
    private Vec3d renderpos = null;

    public final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();
    private final Object2IntMap<UUID> chatIdMap = new Object2IntOpenHashMap<>();

    @Override
    public void onActivate() {
        totemPopMap.clear();
        chatIdMap.clear();

        bootsTask.reset();
        leggingsTask.reset();
        chestplateTask.reset();
        helmetTask.reset();
    }

    @EventHandler
    private void onGameJoin(GameJoinedEvent event) {
        totemPopMap.clear();
        chatIdMap.clear();
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!(event.packet instanceof EntityStatusS2CPacket p)) return;
        if (p.getStatus() != 35) return;
        Entity entity = p.getEntity(mc.world);
        if (!(entity instanceof PlayerEntity)) return;
        if ((entity.equals(mc.player))) return;

        synchronized (totemPopMap) {
            int pops = totemPopMap.getOrDefault(entity.getUuid(), 0);
            totemPopMap.put(entity.getUuid(), ++pops);
            if (totemNotif.get()) send(entity.getEntityName() + " popped " + pops + " time`s!", notifications);

            if (notifications.get().equals(Mode.Toast)) {
                Managers.NOTIFICATION.info(title,entity.getEntityName() + " popped " + pops + " time`s!");
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        synchronized (totemPopMap) {
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (!totemPopMap.containsKey(player.getUuid())) continue;

                if (player.deathTime > 0 || player.getHealth() <= 0) {
                    int pops = totemPopMap.getOrDefault(player.getUuid(), 0);
                    String xx = player.getEntityName() + " just died after " + pops +" pops!";
                    if (deaths.get()) send(xx, notifications);
                    if (notifications.get().equals(Mode.Toast)) {
                        Managers.NOTIFICATION.info(title,xx);
                    }
                    totemPopMap.removeInt(player.getUuid());
                    chatIdMap.removeInt(player.getUuid());
                }
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!armor.get()) return;
        ItemStack boots = mc.player.getInventory().getArmorStack(0);
        ItemStack leggings = mc.player.getInventory().getArmorStack(1);
        ItemStack chestplate = mc.player.getInventory().getArmorStack(2);
        ItemStack helmet = mc.player.getInventory().getArmorStack(3);

        if (boots.isEmpty() && leggings.isEmpty() && chestplate.isEmpty() && helmet.isEmpty()) return;

        if (getPercentage(boots) < percentage.get()) {
            bootsTask.run(() -> notifyArmor("Boots", getPercentage(boots)));
        } else bootsTask.reset();

        if (getPercentage(leggings) < percentage.get()) {
            leggingsTask.run(() -> notifyArmor("Leggings", getPercentage(leggings)));
        } else leggingsTask.reset();

        if (getPercentage(chestplate) < percentage.get()) {
            chestplateTask.run(() -> notifyArmor("Chestplate", getPercentage(chestplate)));
        } else chestplateTask.reset();

        if (getPercentage(helmet) < percentage.get()) {
            helmetTask.run(() -> notifyArmor("Helmet", getPercentage(helmet)));
        } else helmetTask.reset();
    }

    @EventHandler
    public void onBreakPacket(PacketEvent.Receive event) {
        if (surroundBreak.get()) {
            if (event.packet instanceof BlockBreakingProgressS2CPacket bbpp) {
                BlockPos bbp = bbpp.getPos();

                if (bbp.equals(prevBreakPos) && bbpp.getProgress() > 0) return;

                PlayerEntity breakingPlayer = (PlayerEntity) mc.world.getEntityById(bbpp.getEntityId());
                BlockPos playerBlockPos = getBlockPos(mc.player);
                boolean validBlock = isCombatBlock(bbp);

                assert breakingPlayer != null;
                if(breakingPlayer==null)return;
                if (breakingPlayer.equals(mc.player)) return;

                for (CardinalDirection direction : CardinalDirection.values()) {
                    if (validBlock && bbp.equals(playerBlockPos.offset(direction.toDirection()))) notifySurroundBreak(breakingPlayer);
                }

                prevBreakPos = bbp;
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (mc.player == null || mc.world == null || renderpos == null) return;
        renderProgress = fadeTime.get() + renderTime.get();
        if(surroundBreakrender.getDefaultValue() && prevBreakPos != null) {
            renderpos = new Vec3d(prevBreakPos.getX(), prevBreakPos.getY(), prevBreakPos.getZ());
            event.renderer.box(new Box(renderpos.getX(), renderpos.getY(), renderpos.getZ(),
                    renderpos.getX() + 1, renderpos.getY(), renderpos.getZ() + 1),
                new Color(color.get().r, color.get().g, color.get().b, (int) Math.round(color.get().a * Math.min(1, renderProgress / fadeTime.get()))),
                new Color(lineColor.get().r, lineColor.get().g, lineColor.get().b, (int) Math.round(lineColor.get().a * Math.min(1, renderProgress / fadeTime.get()))), shapeMode.get(), 0);
        }
    }

    public void send(String msg, Setting<Mode> notify) {
        switch (notify.get()) {
            case Chat -> info(msg);
            case Toast -> addToast(msg);
        }
    }

    private void notifyArmor(String armor, int percentage) {
        String msg = message.get();

        msg = msg.replace("{armor}", armor);
        msg = msg.replace("{value}", String.valueOf(percentage));
        msg = msg.replace("{%}", String.valueOf(this.percentage.get()));

        if (percentage != 0) send(msg, notifications);

        switch (notifications.get()) {
            case Notification -> Managers.NOTIFICATION.warn(title,msg);
            case Chat -> warning(msg);
        }
    }

    private int getPercentage(ItemStack itemStack) {
        return Math.round(((itemStack.getMaxDamage() - itemStack.getDamage()) * 100f) / (float) itemStack.getMaxDamage());
    }

    private void notifySurroundBreak(PlayerEntity player) {
        String xx ="Your surround is being broken by " + player.getEntityName();

        switch (notifications.get()) {
            case Chat -> ChatUtils.warning(xx);
            case Notification -> Managers.NOTIFICATION.warn(title,xx);
            case Toast -> send(xx, notifications);
        }
    }

    public enum Mode {
        Toast,
        Notification,
        Chat
    }
}
