package tcy.addon.atrihack.modules.conbot;

import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.enums.RotationType;
import tcy.addon.atrihack.enums.SwingHand;
import tcy.addon.atrihack.enums.SwingState;
import tcy.addon.atrihack.enums.SwingType;
import tcy.addon.atrihack.utils.BOInvUtils;
import tcy.addon.atrihack.utils.RotationUtils;
import tcy.addon.atrihack.utils.SettingUtils;
import tcy.addon.atrihack.managers.Managers;
import tcy.addon.atrihack.utils.meteor.BODamageUtils;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author OLEPOSSU
 */

public class KillAuraPlus extends ArkModule {
    public KillAuraPlus() {
        super(ATRIHack.atricombot, "Kill Aura+", "Better kill aura. Made for crystal pvp.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.getDefaultGroup();

    private final Setting<TargetMode> targetMode = sgGeneral.add(new EnumSetting.Builder<TargetMode>()
        .name("Target Mode")
        .description("Which opponent should be targeted.")
        .defaultValue(TargetMode.Health)
        .build()
    );
    private final Setting<Integer> maxHp = sgGeneral.add(new IntSetting.Builder()
        .name("Max HP")
        .description("Target's health must be under this value.")
        .defaultValue(36)
        .min(0)
        .sliderMax(36)
        .build()
    );
    private final Setting<Double> delay = sgGeneral.add(new DoubleSetting.Builder()
        .name("Delay")
        .description("Delay that will be used for hits.")
        .defaultValue(0.500)
        .min(0)
        .sliderMax(1)
        .build()
    );
    private final Setting<RotationMode> rotationMode = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
        .name("Rotation mode")
        .description("When should we rotate. Only active if attack rotations are enabled in rotation settings.")
        .defaultValue(RotationMode.OnHit)
        .build()
    );
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>()
        .name("Switch mode")
        .description("Should be set to disabled.")
        .defaultValue(SwitchMode.Disabled)
        .build()
    );
    private final Setting<Boolean> onlyWeapon = sgGeneral.add(new BoolSetting.Builder()
        .name("Only Weapon")
        .description("Only attacks with a weapon.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("Swing")
        .description("Renders swing animation when attacking an entity.")
        .defaultValue(true)
        .build()
    );
    private final Setting<SwingHand> swingHand = sgGeneral.add(new EnumSetting.Builder<SwingHand>()
        .name("Swing Hand")
        .description("Which hand should be swung.")
        .defaultValue(SwingHand.RealHand)
        .visible(swing::get)
        .build()
    );
    //----------------------------------------------render------------------------------
    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("Shape Mode")
        .description("Which parts of render should be rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("Line Color")
        .description("Line color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 255))
        .build()
    );
    public final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
        .name("Side Color")
        .description("Side color of rendered boxes")
        .defaultValue(new SettingColor(255, 0, 0, 50))
        .build()
    );
    private final Setting<Boolean> renderExt = sgRender.add(new BoolSetting.Builder()
        .name("Render Extrapolation")
        .description("Renders boxes at players' predicted positions.")
        .defaultValue(false)
        .build()
    );
    private final Setting<Boolean> renderSelfExt = sgRender.add(new BoolSetting.Builder()
        .name("Render Self Extrapolation")
        .description("Renders box at your predicted position.")
        .defaultValue(false)
        .build()
    );
    private final Map<AbstractClientPlayerEntity, Box> extPos = new HashMap<>();

    private double timer = 0;
    private PlayerEntity target = null;

    @EventHandler
    private void onRender(Render3DEvent event) {
        timer = Math.min(delay.get(), timer + event.frameTime);

        updateTarget();

        if (target == null) {
            return;
        }

        boolean switched = false;
        switch (switchMode.get()) {
            case Disabled ->
                switched = !onlyWeapon.get() || mc.player.getMainHandStack().getItem() instanceof SwordItem || mc.player.getMainHandStack().getItem() instanceof AxeItem;
            case Normal -> {
                int slot = bestSlot(false);
                if (slot >= 0) {
                    InvUtils.swap(slot, true);
                    switched = true;
                }
            }
            case InvSwitch, PickSwitch, Silent -> switched = true;
        }
        if (mc.player != null) {
            //Render extrapolation
            if (renderExt.get()) {
                extPos.forEach((name, bb) -> {
                    if (renderSelfExt.get() || !name.equals(mc.player))
                        event.renderer.box(bb, color.get(), lineColor.get(), shapeMode.get(), 0);
                });
            }
        }        if (!switched) {
            return;
        }

        boolean rotated = rotationMode.get() != RotationMode.Constant || !SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(target.getBoundingBox(), priority, RotationType.Attacking, Objects.hash(name + "attacking"));

        if (!rotated || timer < delay.get()) {
            return;
        }

        rotated = rotationMode.get() != RotationMode.OnHit || !SettingUtils.shouldRotate(RotationType.Attacking) || Managers.ROTATION.start(target.getBoundingBox(), priority, RotationType.Attacking, Objects.hash(name + "attacking"));

        if (!rotated) {
            return;
        }


        switch (switchMode.get()) {
            case Silent -> {
                switched = false;
                int slot = bestSlot(false);
                if (slot >= 0) {
                    InvUtils.swap(slot, true);
                    switched = true;
                }
            }
            case PickSwitch -> {
                switched = false;
                int slot = bestSlot(true);
                if (slot >= 0) {
                    switched = BOInvUtils.pickSwitch(slot);
                }
            }
            case InvSwitch -> {
                switched = false;
                int slot = bestSlot(true);
                if (slot >= 0) {
                    switched = BOInvUtils.invSwitch(slot);
                }
            }
        }

        if (!switched) {
            return;
        }

        attackTarget();

        switch (switchMode.get()) {
            case Silent -> InvUtils.swapBack();
            case InvSwitch -> BOInvUtils.swapBack();
            case PickSwitch -> BOInvUtils.pickSwapBack();
        }

        if (rotationMode.get() == RotationMode.OnHit) {
            Managers.ROTATION.end(Objects.hash(name + "attacking"));
        }
    }

    private void attackTarget() {
        timer = 0;

        SettingUtils.swing(SwingState.Pre, SwingType.Attacking, Hand.MAIN_HAND);

        sendPacket(PlayerInteractEntityC2SPacket.attack(target, mc.player.isSneaking()));

        SettingUtils.swing(SwingState.Post, SwingType.Attacking, Hand.MAIN_HAND);
        if (swing.get()) clientSwing(swingHand.get(), Hand.MAIN_HAND);
    }

    private int bestSlot(boolean inventory) {
        int slot = -1;
        double hDmg = -1;
        double dmg;
        for (int i = 0; i < (inventory ? mc.player.getInventory().size() + 1 : 9); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (onlyWeapon.get() && !(stack.getItem() instanceof SwordItem) && !(stack.getItem() instanceof AxeItem)) {
                continue;
            }

            dmg = BODamageUtils.getSwordDamage(stack, mc.player, target, true);
            if (dmg > hDmg) {
                slot = i;
                hDmg = dmg;
            }
        }
        return slot;
    }

    private void updateTarget() {
        double value = 0;
        target = null;

        mc.world.getPlayers().forEach(player -> {
            if (player.getHealth() <= 0 || player.isSpectator() || player.getHealth() + player.getAbsorptionAmount() > maxHp.get() || !SettingUtils.inAttackRange(player.getBoundingBox()) || player == mc.player || Friends.get().isFriend(player)) {
                return;
            }

            double val = switch (targetMode.get()) {
                case Health -> 10000 - player.getHealth() - player.getAbsorptionAmount();
                case Angle -> 10000 - Math.abs(RotationUtils.yawAngle(mc.player.getYaw(), Rotations.getYaw(player)));
                case Distance -> 10000 - mc.player.getPos().distanceTo(player.getPos());
            };
            if (val > value) {
                target = player;
            }
        });
    }

    public enum TargetMode {
        Health,
        Angle,
        Distance
    }

    public enum RotationMode {
        OnHit,
        Constant
    }

    public enum SwitchMode {
        Disabled,
        Normal,
        Silent,
        PickSwitch,
        InvSwitch
    }
}
