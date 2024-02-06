package tcy.addon.atrihack.modules;

import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import org.joml.Vector3d;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;
import tcy.addon.atrihack.enums.RotationType;
import tcy.addon.atrihack.enums.SwingHand;
import tcy.addon.atrihack.managers.Managers;
import tcy.addon.atrihack.modules.conbot.AutoCrystalPlus;
import tcy.addon.atrihack.utils.*;
import tcy.addon.atrihack.utils.meteor.BODamageUtils;

import java.util.*;

public class AnchorAuraPlus extends ArkModule {
    public AnchorAuraPlus() {
        super(ATRIHack.atricombot, "Anchor Aura+", "Automatically destroys people using anchors.");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDamage = settings.createGroup("Damage");
    private final SettingGroup sgRender = settings.createGroup("Render");
    private final SettingGroup sgText = settings.createGroup("Text");
    private final SettingGroup sgcro = settings.createGroup("Crosshair");

    //--------------------General--------------------//
    private final Setting<Boolean> pauseEat = sgGeneral.add(new BoolSetting.Builder().name("Pause Eat").description("Pauses when you are eating.").defaultValue(true).build());
    private final Setting<Boolean> Cev = sgGeneral.add(new BoolSetting.Builder().name("AutoAnchorCev").description("").defaultValue(true).build());
    private final Setting<SwitchMode> switchMode = sgGeneral.add(new EnumSetting.Builder<SwitchMode>().name("Switch Mode").description("Switching method. Silent is the most reliable but doesn't work everywhere.").defaultValue(SwitchMode.Silent).build());
    private final Setting<LogicMode> logicMode = sgGeneral.add(new EnumSetting.Builder<LogicMode>().name("Logic Mode").description("Logic for bullying kids.").defaultValue(LogicMode.ATRI0tick).build());
    private final Setting<Integer> maxtarget = sgGeneral.add(new IntSetting.Builder().name("MaxTarget").description("").defaultValue(2).min(0).sliderRange(0, 5).build());

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder().name("Speed").description("How many anchors should be blown every second.").defaultValue(2).min(0).sliderRange(0, 20).build());
    private final Setting<Double> lowspeed = sgGeneral.add(new DoubleSetting.Builder().name("Speed-Low Health").description("How many anchors should be blown every second.").defaultValue(2).min(0).sliderRange(0, 20).build());
    private final Setting<Boolean> smartSpeed = sgGeneral.add(new BoolSetting.Builder().name("Smart speed").description("").defaultValue(true).build());
    private final Setting<Boolean> cevhelper = sgGeneral.add(new BoolSetting.Builder().name("Debug").description(".").defaultValue(false).build());
    private final Setting<Boolean> changespeed = sgGeneral.add(new BoolSetting.Builder().name("change speed").description("").defaultValue(true).build());
    private final Setting<Double> minhealth = sgDamage.add(new DoubleSetting.Builder().name("Min Health").description("").defaultValue(6).min(0).sliderRange(0, 20).build());
    private final Setting<Boolean> debug = sgGeneral.add(new BoolSetting.Builder().name("Debug").description(".").defaultValue(false).build());
    //--------------------Damage--------------------//
    private final Setting<Double> minDmg = sgDamage.add(new DoubleSetting.Builder().name("Min Damage").description("Minimum damage required to place.").defaultValue(8).min(0).sliderRange(0, 20).build());
    private final Setting<Double> maxDmg = sgDamage.add(new DoubleSetting.Builder().name("Max Damage").description("Maximum damage to self.").defaultValue(6).min(0).sliderRange(0, 20).build());
    private final Setting<Double> minRatio = sgDamage.add(new DoubleSetting.Builder().name("Min Damage Ratio").description("Damage ratio between enemy damage and self damage (enemy / self).").defaultValue(2).min(0).sliderRange(0, 10).build());
    private final Setting<Boolean> collision = sgDamage.add(new BoolSetting.Builder().name("collision").description("").defaultValue(true).build());
    private final Setting<Integer> predict_tick = sgDamage.add(new IntSetting.Builder().name("predict_tick").description("").defaultValue(4).min(0).sliderRange(0, 50).build());
    //--------------------Text--------------------//
    private final Setting<Double> textScale = sgText.add(new DoubleSetting.Builder().name("Text Scale").description(".").defaultValue(3).range(0, 10).sliderRange(0, 10).build());
    private final Setting<SettingColor> textColor = sgText.add(new ColorSetting.Builder().name("Text Color").description(ATRIHack.COLOR).defaultValue(new SettingColor(255, 255, 255, 50)).build());
    private final Setting<Boolean> shadow = sgText.add(new BoolSetting.Builder().name("Shadow").description("Do text shadow render.").defaultValue(true).build());
    //--------------------Render--------------------//
    private final Setting<Boolean> placeSwing = sgRender.add(new BoolSetting.Builder().name("Place Swing").description("Renders swing animation when placing a block.").defaultValue(true).build());
    private final Setting<SwingHand> placeHand = sgRender.add(new EnumSetting.Builder<SwingHand>().name("Place Hand").description("Which hand should be swung.").defaultValue(SwingHand.RealHand).visible(placeSwing::get).build());
    private final Setting<Boolean> interactSwing = sgRender.add(new BoolSetting.Builder().name("Interact Swing").description("Renders swing animation when interacting with a block.").defaultValue(true).build());
    private final Setting<SwingHand> interactHand = sgRender.add(new EnumSetting.Builder<SwingHand>().name("Interact Hand").description("Which hand should be swung.").defaultValue(SwingHand.RealHand).visible(interactSwing::get).build());
    private final Setting<Boolean> targetname = sgRender.add(new BoolSetting.Builder().name("TargetName").description("Renders swing animation when placing a block.").defaultValue(false).build());
    private final Setting<RenderMode> renderMode = sgRender.add(new EnumSetting.Builder<RenderMode>().name("Render Mode").description("The mode to render in.").defaultValue(RenderMode.Normal).build());
    private final Setting<FadeMode> fadeMode = sgRender.add(new EnumSetting.Builder<FadeMode>().name("Fade Mode").description("How long the fading should take.").defaultValue(FadeMode.Normal).visible(() -> renderMode.get() == RenderMode.Test).build());
    private final Setting<Double> animationSpeed = sgRender.add(new DoubleSetting.Builder().name("Animation Move Speed").description("How fast should boze mode box move.").defaultValue(1).min(0).sliderRange(0, 10).visible(() -> renderMode.get().equals(RenderMode.Test)).build());
    private final Setting<Double> animationMoveExponent = sgRender.add(new DoubleSetting.Builder().name("Animation Move Exponent").description("Moves faster when longer away from the target.").defaultValue(2).min(0).sliderRange(0, 10).visible(() -> renderMode.get().equals(RenderMode.Test)).build());
    private final Setting<Double> animationExponent = sgRender.add(new DoubleSetting.Builder().name("Animation Exponent").description("How fast should boze mode box grow.").defaultValue(3).min(0).sliderRange(0, 10).visible(() -> renderMode.get().equals(RenderMode.Test)).build());
    private final Setting<Integer> renderTime = sgRender.add(new IntSetting.Builder().name("render-time").description("How long to render placements.").defaultValue(10).min(0).sliderMax(20).visible(() -> renderMode.get() == RenderMode.Test).build());
    public final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>().name("Shape Mode").description("Which parts should be renderer.").defaultValue(ShapeMode.Both).build());
    public final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder().name("Side Color").description("Side color of rendered stuff").defaultValue(new SettingColor(255, 0, 0, 50)).build());
    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder().name("Line Color").description("Line color of rendered stuff").defaultValue(new SettingColor(255, 0, 0, 255)).build());
//---------------------------------------------cro-------------------------------
    private final Setting<SettingColor> color = sgcro.add(new ColorSetting.Builder().name("crocolor").description("").defaultValue(new SettingColor(0x2250b4b4)).build());


    private BlockPos[] blocks = new BlockPos[]{};
    private int lastIndex = 0;
    private int length = 0;
    private long tickTime = -1;
    private double bestDmg = -1;
    private long lastTime = 0;

    private BlockPos placePos = null;
    private PlaceData placeData = null;
    private BlockPos calcPos = null;
    private PlaceData calcData = null;
    private BlockPos renderPos = null;
    private Vec3d renderTarget = null;
    private Vec3d renderPos0 = null;
    private Box renderBox = null;
    private List<PlayerEntity> targets = new ArrayList<>(maxtarget.get());
    private final Map<BlockPos, Anchor> anchors = new HashMap<>();

    double timer = 0;
    private double renderProgress = 0;

    public enum LogicMode {
        ATRI0tick
    }
    public enum SwitchMode {
        Silent,
        Normal,
        PickSilent,
        InvSwitch,
        Disabled
    }
    public enum AnchorState {
        Air,
        Anchor,
        Loaded
    }
    public enum RenderMode {
        Normal,
        Test
    }

    @Override
    public void onActivate() {
        renderProgress = 0;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onTickPre(TickEvent.Post event) {
        calculate(length - 1);
        renderPos = calcPos;
        placePos = calcPos;
        placeData = calcData;
        blocks = getBlocks(mc.player.getEyePos(), Math.max(SettingUtils.getPlaceRange(), SettingUtils.getPlaceWallsRange()));

        // Reset stuff
        tickTime = System.currentTimeMillis();
        length = blocks.length;
        lastIndex = 0;
        bestDmg = -1;
        calcPos = null;
        calcData = null;

        updateTargets();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onRender(Render3DEvent event) {
        double delta = (System.currentTimeMillis() - lastTime) / 1000f;
        timer += delta;

        lastTime = System.currentTimeMillis();
        if (tickTime < 0 || mc.player == null || mc.world == null) {
            return;
        }

        if (shouldPause()) {
            update();
        }

        List<BlockPos> toRemove = new ArrayList<>();
        anchors.forEach((pos, anchor) -> {
            if (System.currentTimeMillis() - anchor.time > 500) {
                toRemove.add(pos);
            }
        });
        toRemove.forEach(anchors::remove);

        int index = Math.min((int) Math.ceil((System.currentTimeMillis() - tickTime) / 50f * length), length - 1);
        calculate(index);

        if (renderMode.get().equals(RenderMode.Normal)) {
            if (renderPos != null && shouldPause()) event.renderer.box(renderPos, sideColor.get(), lineColor.get(), shapeMode.get(), 0);
        }
        if (renderMode.get().equals(RenderMode.Test)) {
            if (renderPos != null && shouldPause()) {
                renderProgress = Math.min(1, renderProgress + delta);
                renderTarget = new Vec3d(renderPos.getX(), renderPos.getY(), renderPos.getZ());
            } else {
                renderProgress = Math.max(0, renderProgress - delta);
            }

            if (renderTarget != null) {
                renderPos0 = smoothMove(renderPos0, renderTarget, delta * animationSpeed.get() * 5);
            }
            if (renderPos0 != null) {
                double r = 0.5 - Math.pow(1 - renderProgress, animationExponent.get()) / 2f;

                if (r >= 0.001) {
                    double down = -0.5;
                    double up = -0.5;
                    double width = 0.5;
                    switch (fadeMode.get()) {
                        case Up -> {
                            up = 0;
                            down = -(r * 2);
                        }
                        case Down -> {
                            up = -1 + r * 2;
                            down = -1;
                        }
                        case Normal -> {
                            up = -0.5 + r;
                            down = -0.5 - r;
                            width = r;
                        }
                    }
                    renderBox = new Box(renderPos0, new Vec3d(renderPos0.getX() + 1, renderPos0.getY() + 1, renderPos0.getZ() + 1));

                    Box box = new Box(renderPos0.getX() + 0.5 - width, renderPos0.getY() + down +1 , renderPos0.getZ() + 0.5 - width,
                        renderPos0.getX() + 0.5 + width, renderPos0.getY() + up + 1, renderPos0.getZ() + 0.5 + width);

                    event.renderer.box(box, injectAlpha(sideColor.get(), MathHelper.clamp((int) (r * 255), 0, sideColor.get().a)), injectAlpha(lineColor.get(), MathHelper.clamp((int) (r * 255), 0, lineColor.get().a)), shapeMode.get(), 0);
                }
            }
        }
    }
    private double getProgress(double delta) {
        return 1 - Math.pow(1 - (delta), 5);
    }

    public static Color injectAlpha(final Color color, final int alpha) {
        int alph = MathHelper.clamp(alpha, 0, 255);
        return new Color(color.r, color.g, color.b, alph);
    }

    private Vec3d smoothMove(Vec3d current, Vec3d target, double delta) {
        if (current == null) return target;

        double absX = Math.abs(current.x - target.x);
        double absY = Math.abs(current.y - target.y);
        double absZ = Math.abs(current.z - target.z);

        double x = (absX + Math.pow(absX, animationMoveExponent.get() - 1)) * delta;
        double y = (absX + Math.pow(absY, animationMoveExponent.get() - 1)) * delta;
        double z = (absX + Math.pow(absZ, animationMoveExponent.get() - 1)) * delta;

        return new Vec3d(current.x > target.x ? Math.max(target.x, current.x - x) : Math.min(target.x, current.x + x),
            current.y > target.y ? Math.max(target.y, current.y - y) : Math.min(target.y, current.y + y),
            current.z > target.z ? Math.max(target.z, current.z - z) : Math.min(target.z, current.z + z));
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        String name="";
        for (PlayerEntity player : targets) {
            name = player.getName().toString();
        }
        if (renderPos == null) return;

        Vector3d vec3 = new Vector3d();

       if (renderMode.get() == RenderMode.Normal){
            vec3.set(renderPos.getX() + 0.5, renderPos.getY() + 0.5, renderPos.getZ() + 0.5);
        } else if (renderMode.get() == RenderMode.Test){
            if(renderBox!=null) {
                vec3.set(renderBox.minX + 0.5, renderBox.minY + 0.5, renderBox.minZ + 0.5);
            }
        }
        int progress = (int) (360 * (mc.player.handSwingProgress));
        progress = progress == 0 ? 360 : progress;

        RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 0, 360, 4f, new SettingColor(0,0,0,255));
        RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 0, 360, 3.5f, new SettingColor(0,0,0,255));
        RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 4f, color.get());
        RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 3.5f, color.get());

        if (NametagUtils.to2D(vec3, textScale.get())) {
            TextRenderer textRenderer = TextRenderer.get();

            NametagUtils.begin(vec3);
            textRenderer.begin(1, false, true);
            if(targetname.get()) {
                TextRenderer font = TextRenderer.get();
                font.render(name, -(font.getWidth(name) / 2), -(font.getHeight()), new Color(255, 63, 82, 200), shadow.get());
            }
            String text = String.format("%.1f", getDmg(renderPos));
            textRenderer.render(
                text,
                -textRenderer.getWidth(text + "%") / 2.0,
                0.0,
                textColor.get(),
                shadow.get()
            );

            textRenderer.end();
            NametagUtils.end();
        }
    }

    private boolean shouldPause() {
        return !pauseEat.get() || !mc.player.isUsingItem();
    }

    private void calculate(int index) {
        BlockPos pos;

        double targetDamage;
        double selfDamage;
        for (int i = lastIndex; i < index; i++) {
            pos = blocks[i];

            targetDamage = getDmg(pos);
            selfDamage = BODamageUtils.anchorDamage(mc.player, mc.player.getBoundingBox(),pos, pos.toCenterPos());

            if (!dmgCheck(targetDamage, selfDamage)) {
                continue;
            }

            PlaceData data = SettingUtils.getPlaceData(pos);

            if (!data.valid()) {
                continue;
            }

            if (EntityUtils.intersectsWithEntity(new Box(pos), entity -> !(entity instanceof ItemEntity))) {
                continue;
            }

            calcData = data;
            calcPos = pos;
            bestDmg = targetDamage;
        }
        lastIndex = index;
    }

    private void updateTargets() {
        List<PlayerEntity> players = new ArrayList<>();
        double closestDist = 1000;
        PlayerEntity closest;
        double dist;
        targets = null;
        for (int i = 3; i > 0; i--) {
            closest = null;
            for (PlayerEntity player : mc.world.getPlayers()) {
                if (players.contains(player) || Friends.get().isFriend(player) || player == mc.player) {
                    continue;
                }

                dist = player.distanceTo(mc.player);

                if (dist > 15) {
                    continue;
                }

                if (closest == null || dist < closestDist) {
                    closestDist = dist;
                    closest = player;
                }
            }
            if (closest != null) {
                players.add(closest);
            }
        }
        targets = players;
    }

    private BlockPos[] getBlocks(Vec3d middle, double radius) {
        ArrayList<BlockPos> result = new ArrayList<>();
        int i = (int) Math.ceil(radius);
        BlockPos pos;

        for (int x = -i; x <= i; x++) {
            for (int y = -i; y <= i; y++) {
                for (int z = -i; z <= i; z++) {
                    pos = new BlockPos((int) (Math.floor(middle.x) + x), (int) (Math.floor(middle.y) + y), (int) (Math.floor(middle.z) + z));

                    if (!LemonUtils.replaceable(pos) && !(mc.world.getBlockState(pos).getBlock() == Blocks.RESPAWN_ANCHOR)) {
                        continue;
                    }

                    if (!inRangeToTargets(pos)) {
                        continue;
                    }

                    if (!SettingUtils.inPlaceRange(pos)) {
                        continue;
                    }

                    result.add(pos);
                }
            }
        }
        return result.toArray(new BlockPos[0]);
    }

    private boolean inRangeToTargets(BlockPos pos) {
            for (PlayerEntity target : targets) {
                if (target.getPos().add(0, 1, 0).distanceTo(Vec3d.ofCenter(pos)) < 3.5) return true;
            }
        return false;
    }

    private void update() {
        if (placePos == null || placeData == null || !placeData.valid()) return;

        Anchor anchor = getAnchor(placePos);

        float heatlth = 0;
        String name="";
        for (PlayerEntity player : targets) {
            heatlth = player.getHealth();
            name = player.getName().toString();
        }

//        {switch (anchor.state) {
//            case Anchor -> {
//                if (chargeUpdate(placePos)) {
//                    Anchor a = new Anchor(AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());
//                    anchors.remove(placePos);
//                    anchors.put(placePos, a);
//                }
//            }
//            case Loaded -> {
//                if (explodeUpdate(placePos)) {
//                    anchors.remove(placePos);
//                    anchors.put(placePos, new Anchor(AnchorState.Air, 0, System.currentTimeMillis()));
//                }
//            }
//            case Air -> {
//                if (timer <= 1 / speed.get()) {
//                    return;
//                }
//
//                if (placeUpdate()) {
//                    anchors.remove(placePos);
//                    anchors.put(placePos, new Anchor(AnchorState.Anchor, 0, System.currentTimeMillis()));
//                    timer = 0;
//                }
//            }
//        }}

        double tspeed = speed.get();
        if (logicMode.get() == LogicMode.ATRI0tick) {
            if (SurCheck(placePos)==0) {
                if (timer <= 1 / tspeed) {
                    return;
                }
                if (smartSpeed.get()) {
                    if (heatlth <= 4) {
                        tspeed = lowspeed.get();
                    } else {
                        tspeed = speed.get();
                    }
                }
                if (Cev.get()) {
                    CevUpdate(placePos);
                }

                if (placeUpdate()) {
                    //anchors.remove(placePos);
                    anchors.put(placePos, new Anchor(AnchorState.Anchor, 1, System.currentTimeMillis()));
                    anchors.remove(placePos);
                }
                if (chargeUpdate(placePos)) {
                    Anchor a = new Anchor(AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());

                    anchors.remove(placePos);
                    anchors.put(placePos, a);
                }
                if (explodeUpdate(placePos)) {
                    //anchors.remove(placePos);
                    anchors.put(placePos, new Anchor(AnchorState.Air, 0, System.currentTimeMillis()));
                    timer = 0;
                    InvUtils.swapBack();

                    if (debug.get()) {
                        sendDisableMsg("PlacePos:" + placePos.getX() + placePos.getZ() + placePos.getY() + "  dmg:" + getDmg(placePos));
                        sendDisableMsg("surcheck 0");
                    }
                }
            }
            }else {
                if (timer <= 1 / tspeed) {
                    return;
                }
                if (smartSpeed.get()) {
                    if (heatlth <= 4) {
                        tspeed = lowspeed.get();
                    } else {
                        tspeed = speed.get();
                    }
                }
                if (Cev.get()) {
                    if (CevUpdate(placePos) == 1){
                        if (placeUpdate()) {
                            //anchors.remove(placePos);
                            anchors.put(placePos, new Anchor(AnchorState.Anchor, 1, System.currentTimeMillis()));
                            anchors.remove(placePos);
                        }
                    }
                }
                    if (chargeUpdate(placePos)) {
                        Anchor a = new Anchor(AnchorState.Loaded, anchor.charges + 1, System.currentTimeMillis());

                        anchors.remove(placePos);
                        anchors.put(placePos, a);
                    }
                    if (explodeUpdate(placePos)) {
                        //anchors.remove(placePos);
                        anchors.put(placePos, new Anchor(AnchorState.Air, 0, System.currentTimeMillis()));
                        timer = 0;
                        InvUtils.swapBack();
                    }
                    if (placeUpdate()) {
                        //anchors.remove(placePos);
                        anchors.put(placePos, new Anchor(AnchorState.Anchor, 1, System.currentTimeMillis()));
                        anchors.remove(placePos);
                    }

                }
            }


    private int CevUpdate(BlockPos pos) {
        for (PlayerEntity target : targets) {
            pos = new BlockPos((int) target.getX(), (int) (target.getY()+2), (int) target.getZ());
        }
        if(pos!=null) {
            if ((getDmg(pos) >= getDmg(placePos)) && mc.world.isAir(pos) && Cev.get()) {
                if (mc.world.getBlockState(BlockPos.fromLong(pos.getX() + 1)).getBlock() != Blocks.AIR || mc.world.getBlockState(BlockPos.fromLong(pos.getX() - 1)).getBlock() != Blocks.AIR || mc.world.getBlockState(BlockPos.fromLong(pos.getY() - 1)).getBlock() != Blocks.AIR || mc.world.getBlockState(BlockPos.fromLong(pos.getY() + 1)).getBlock() != Blocks.AIR) {
                    placePos = pos;
                    if(cevhelper.get()){
                        BlockUtils.place(pos.add(0, 1, 0), InvUtils.findInHotbar(Items.COBWEB), 0, false);
                    }
                    if (debug.get()) {
                        sendToggledMsg("cevUpdate :" + pos);
                    }
                }
            }
            return 1;
        }
        return 0;
    }
    private int SurCheck(BlockPos pos){
        for (PlayerEntity target : targets) {
            pos = new BlockPos((int) target.getX(), (int) (target.getY()), (int) target.getZ());
        }
        if(pos==null) return 0;
        if(placePos==BlockPos.fromLong(pos.getX()+1) || placePos==BlockPos.fromLong(pos.getX()-1) || placePos==BlockPos.fromLong(pos.getY()+1) || placePos==BlockPos.fromLong(pos.getY()-1)){
            return 1;
        }

        return 0;
    }

        private Anchor getAnchor(BlockPos pos) {
        if (anchors.containsKey(pos)) {
            return anchors.get(pos);
        }
        BlockState state = mc.world.getBlockState(pos);
        return new Anchor(state.getBlock() == Blocks.RESPAWN_ANCHOR ? state.get(Properties.CHARGES) < 1 ? AnchorState.Anchor : AnchorState.Loaded : AnchorState.Air, state.getBlock() == Blocks.RESPAWN_ANCHOR ? state.get(Properties.CHARGES) : 0, System.currentTimeMillis());
    }

    private boolean placeUpdate() {
        Hand hand = Managers.HOLDING.isHolding(Items.RESPAWN_ANCHOR) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() == Items.RESPAWN_ANCHOR ? Hand.OFF_HAND : null;

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = result.found();

                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.BlockPlace) && !Managers.ROTATION.start(placeData.pos(), priority, RotationType.BlockPlace, Objects.hash(name + "placing"))) {
            return false;
        }


        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.RESPAWN_ANCHOR);
                    InvUtils.swap(result.slot(),true);
                }
                case PickSilent -> {
                    FindItemResult result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = BOInvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.RESPAWN_ANCHOR);
                    switched = BOInvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        Hand shouldUseHand = (hand == null ? Hand.MAIN_HAND : hand);
        placeBlock(shouldUseHand, placeData.pos().toCenterPos(), placeData.dir(), placeData.pos());
        if (placeSwing.get()) clientSwing(placeHand.get(), shouldUseHand);

        if (SettingUtils.shouldRotate(RotationType.BlockPlace)) {
            Managers.ROTATION.end(Objects.hash(name + "placing"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
        return true;
    }

    private boolean chargeUpdate(BlockPos pos) {
        Hand hand = Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() == Items.GLOWSTONE ? Hand.OFF_HAND : null;
        Direction dir = SettingUtils.getPlaceOnDirection(pos);

        if (dir == null) {
            return false;
        }

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.GLOWSTONE);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.GLOWSTONE);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(pos, priority, RotationType.Interact, Objects.hash(name + "interact"))) {
            return false;
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(Items.GLOWSTONE);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    FindItemResult result = InvUtils.find(Items.GLOWSTONE);
                    switched = BOInvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    FindItemResult result = InvUtils.find(Items.GLOWSTONE);
                    switched = BOInvUtils.invSwitch(result.slot());
                }

            }
        }

        if (!switched) {
            return false;
        }

        interact(pos, dir, hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(Objects.hash(name + "interact"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
        return true;
    }

    private boolean explodeUpdate(BlockPos pos) {
        Hand hand = !Managers.HOLDING.isHolding(Items.GLOWSTONE) ? Hand.MAIN_HAND : mc.player.getOffHandStack().getItem() != Items.GLOWSTONE ? Hand.OFF_HAND : null;
        Direction dir = SettingUtils.getPlaceOnDirection(pos);

        if (dir == null) {
            return false;
        }

        boolean switched = hand != null;

        if (!switched) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(stack -> stack.getItem() != Items.GLOWSTONE);
                    switched = result.found();
                }
                case PickSilent, InvSwitch -> {
                    FindItemResult result = InvUtils.find(stack -> stack.getItem() != Items.GLOWSTONE);
                    switched = result.found();
                }
            }
        }

        if (!switched) {
            return false;
        }

        if (SettingUtils.shouldRotate(RotationType.Interact) && !Managers.ROTATION.start(pos, priority, RotationType.Interact, Objects.hash(name + "explode"))) {
            return false;
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent, Normal -> {
                    FindItemResult result = InvUtils.findInHotbar(item -> item.getItem() != Items.GLOWSTONE);
                    InvUtils.swap(result.slot(), true);
                }
                case PickSilent -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() != Items.GLOWSTONE);
                    switched = BOInvUtils.pickSwitch(result.slot());
                }
                case InvSwitch -> {
                    FindItemResult result = InvUtils.find(item -> item.getItem() != Items.GLOWSTONE);
                    switched = BOInvUtils.invSwitch(result.slot());
                }
            }
        }

        if (!switched) {
            return false;
        }

        interact(pos, dir, hand == null ? Hand.MAIN_HAND : hand);

        if (SettingUtils.shouldRotate(RotationType.Interact)) {
            Managers.ROTATION.end(Objects.hash(name + "explode"));
        }

        if (hand == null) {
            switch (switchMode.get()) {
                case Silent -> InvUtils.swapBack();
                case PickSilent -> BOInvUtils.pickSwapBack();
                case InvSwitch -> BOInvUtils.swapBack();
            }
        }
        return true;
    }
    public enum FadeMode {
        Up,
        Down,
        Normal
    }
    private void interact(BlockPos pos, Direction dir, Hand hand) {
        interactBlock(hand, pos.toCenterPos(), dir, pos);

        if (interactSwing.get()) clientSwing(interactHand.get(), hand);
    }

    private boolean dmgCheck(double dmg, double self) {
        if (dmg < bestDmg) {
            return false;
        }

        if (dmg < minDmg.get()) {
            return false;
        }
        if (self > maxDmg.get()) {
            return false;
        }
        if (dmg / self < minRatio.get()) {
            return false;
        }

        return true;
    }

    private double getDmg(BlockPos pos) {
        double highest = -1;
        for (PlayerEntity target : targets) {
            highest = Math.max(highest,BOInvUtils.getAnchorDamage(pos,target,predict_tick.get(),collision.get()));
        }
        return highest;
    }

    private record Anchor(AnchorState state, int charges, long time) {}
}
