package tcy.addon.atrihack;

import com.google.common.eventbus.Subscribe;
import com.mojang.logging.LogUtils;

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript;
import meteordevelopment.starscript.value.ValueMap;
import org.slf4j.LoggerFactory;
import tcy.addon.atrihack.commands.BlackoutGit;
import tcy.addon.atrihack.commands.Coords;
import tcy.addon.atrihack.commands.NotificationsCommand;
import tcy.addon.atrihack.events.UpdateEvent;
import tcy.addon.atrihack.globalsettings.*;
import tcy.addon.atrihack.hud.*;
import tcy.addon.atrihack.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import tcy.addon.atrihack.modules.conbot.*;
import tcy.addon.atrihack.modules.crash.*;
import tcy.addon.atrihack.modules.misc.*;
import tcy.addon.atrihack.modules.mov.*;
import tcy.addon.atrihack.modules.player.*;
import tcy.addon.atrihack.modules.player.timer.TimerPlus;
import tcy.addon.atrihack.modules.render.*;
import tcy.addon.atrihack.utils.StatsUtils;
import tcy.addon.hwid.Hwid;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static meteordevelopment.meteorclient.MeteorClient.mc;



public class ATRIHack extends MeteorAddon {
    public static final Logger LOGGER = LoggerFactory.getLogger("example");
    public static final Logger LOG = LogUtils.getLogger();



    public static final Category ArkMode = new Category("ATRIHack", Items.END_CRYSTAL.getDefaultStack());
    public static final Category atricombot = new Category("ATRIcombot", Items.DIAMOND_SWORD.getDefaultStack());
    public static final Category atrirender = new Category("ATRIrender", Items.ACACIA_LEAVES.getDefaultStack());
    public static final Category atrimisc = new Category("ATRImisc", Items.ACACIA_BUTTON.getDefaultStack());
    public static final Category Crash = new Category("ATRICrash", Items.TNT.getDefaultStack());
    public static final Category SETTINGS = new Category("Settings", Items.OBSIDIAN.getDefaultStack());
    public static final HudGroup HUD_BLACKOUT = new HudGroup("ATRIHack");
    public static final HudGroup HUD_GROUP = new HudGroup("Notifications");

    public static final String BLACKOUT_NAME = "ATRIHack";
    public static final String BLACKOUT_VERSION = "2024";
    public static final String COLOR = "Color is the visual perception of different wavelengths of light as hue, saturation, and brightness";
    private TrayIcon icon;

    @Override
    public void onInitialize() {



        LOG.info("Initializing ATRIHack");

        initializeModules(Modules.get());

        initializeSettings(Modules.get());

        initializeCommands();

        Hud.get().register(NotificationsHudElement.INFO);

        initializeHud(Hud.get());
        MeteorStarscript.ss.set("atrihack", new ValueMap()
            .set("kills", StatsUtils::getKills)
            .set("deaths", StatsUtils::getDeaths)
            .set("kdr", StatsUtils::getKDR)
            .set("killstreak", StatsUtils::getKillstreak)
            .set("highscore", StatsUtils::getHighscore)
            .set("crystalsps", StatsUtils::getCrystalsPs)
            .set("anchorps", StatsUtils::getAnchorPs)

        );
    }

    @Subscribe
    public void onUpdate(UpdateEvent event) {
        mc.getWindow().setTitle("ATRI Hack | \u65f6\u95f4\u6d41\u901d\u5427\uff0c\u4f60\u662f\u591a\u4e48\u6b8b\u9177");
        this.icon.displayMessage("ATRIHack", "\u50cf\u62fc\u56fe\u822c\u8054\u7cfb\u7ed3\u5408\u6210\u7684\u672a\u6765,\u6211\u6240\u9057\u5931\u7684,\u662f\u54ea\u4e00\u7247", TrayIcon.MessageType.INFO);
    }

    private void initializeModules(Modules modules) {
        modules.add(new AnchorAuraPlus());
        modules.add(new AnteroTaateli());
        modules.add(new AntiAim());
        modules.add(new AntiCrawl());
        modules.add(new AutoCraftingTable());
        modules.add(new AutoCrystalPlus());
        modules.add(new AutoEz());
        modules.add(new Automation());
        modules.add(new AutoMend());
        modules.add(new AutoMine());
        modules.add(new AutoMoan());
        modules.add(new AutoPearl());
        modules.add(new AutoPvp());
        modules.add(new AutoTrapPlus());
        modules.add(new Blocker());
        modules.add(new BurrowPlus());
        modules.add(new CustomFOV());
        modules.add(new ElytraFlyPlus());
        modules.add(new FeetESP());
        modules.add(new FlightPlus());
        modules.add(new Fog());
        modules.add(new ForceSneak());
        modules.add(new HoleFillPlus());
        modules.add(new HoleFillRewrite());
        modules.add(new HoleSnap());
        modules.add(new JesusPlus());
        modules.add(new KillAuraPlus());
        modules.add(new LightsOut());
        modules.add(new MineESP());
        modules.add(new OffHandPlus());
        modules.add(new PacketFly());
        modules.add(new PacketLogger());
        modules.add(new PingSpoof());
        modules.add(new PistonCrystal());
        modules.add(new PistonPush());
        modules.add(new PortalGodMode());
        modules.add(new RPC());
        modules.add(new ScaffoldPlus());
        modules.add(new SelfTrapPlus());
        modules.add(new SoundModifier());
        modules.add(new SpeedPlus());
        modules.add(new SprintPlus());
        modules.add(new StepPlus());
        modules.add(new StrictNoSlow());
        modules.add(new Suicide());
        modules.add(new SurroundPlus());
        modules.add(new SwingModifier());
        modules.add(new TickShift());
        modules.add(new WeakAlert());
        modules.add(new PacketEat());
        modules.add(new BreakCrystal());
        /*-----------------------------------------new-----------------------------------*/
        modules.add(new AimAssist());
        modules.add(new BedBombV4());
        modules.add(new TNTAura());
        modules.add(new AutoCraft());
        modules.add(new BedCrafter());
        modules.add(new CityMiner());
        modules.add(new CityBreaker());
        modules.add(new KillEffects());
        modules.add(new GroupChat());
        modules.add(new SwingAnimation());
        modules.add(new ShieldBypass());
        modules.add(new Notifications());
        modules.add(new EyeFinder());
        modules.add(new Airstrike());
        modules.add(new PenisESP());
        modules.add(new AntiNbtBypasser());
        modules.add(new phase());
        modules.add(new SitModule());
        modules.add(new WorldGuardBypass());
        modules.add(new PermJukebox());
        modules.add(new SkeletonESP());
        modules.add(new InstaMine());
        modules.add(new HoleRenderer());
        modules.add(new GhostMode());
        modules.add(new PacketDigits());
        modules.add(new EdgeJump());
        modules.add(new EntityPhase());
        modules.add(new NoCollision());
        modules.add(new Nametags());
        modules.add(new PlayerDev());
        modules.add(new HitSound());
        modules.add(new AutoCityPlus());
        modules.add(new AntiPiston());
        modules.add(new BurrowRender());
        modules.add(new SimpleNameTag());
        modules.add(new PlaceAntiKicker());
        modules.add(new BowMcBomb());
        modules.add(new AACCrash());
        modules.add(new BookCrash());
        modules.add(new ContainerCrash());
        modules.add(new CraftingCrash());
        modules.add(new CreativeCrash());
        modules.add(new EntityCrash());
        modules.add(new ErrorCrash());
        modules.add(new InteractCrash());
        modules.add(new LecternCrash());
        modules.add(new MessageLagger());
        modules.add(new MovementCrash());
        modules.add(new PacketSpammer());
        modules.add(new SequenceCrash());
        modules.add(new BlockSelectionP());
        modules.add(new PlaceRender());
        modules.add(new ChorusRender());
        modules.add(new SoundRender());
        modules.add(new ItemESP());
        modules.add(new extrapolateESP());
        modules.add(new WebNoSlow());
        modules.add(new AutoCrystalRewrite());
        modules.add(new AutoFollow());
        modules.add(new ItemSucker());
        modules.add(new EntityUse());
        modules.add(new RemindESP());
        modules.add(new HoleESPPlus());
        modules.add(new StrafePlus());
        modules.add(new InstaMinePlus());
        modules.add(new TimeAnimator());
        modules.add(new ChatSuffix());
        modules.add(new CityESPPlus());
        modules.add(new AutoFishPlus());
        modules.add(new TimerPlus());
        modules.add(new AutoWebPlus());
        modules.add(new InventoryScroll());
        modules.add(new ItemRelease());
        modules.add(new TotemPops());
        modules.add(new CrystalESP());
    }

    private void initializeSettings(Modules modules) {
        modules.add(new FacingSettings());
        modules.add(new RangeSettings());
        modules.add(new RaytraceSettings());
        modules.add(new RotationSettings());
        modules.add(new ServerSettings());
        modules.add(new SwingSettings());
    }

    private void initializeCommands() {
        Commands.add(new BlackoutGit());
        Commands.add(new Coords());
        Commands.add(new NotificationsCommand());

    }

    private void initializeHud(Hud hud) {
        hud.register(ArmorHudPlus.INFO);
        hud.register(BlackoutArray.INFO);
        hud.register(GearHud.INFO);
        hud.register(HudWaterMark.INFO);
        hud.register(Keys.INFO);
        hud.register(TargetHud.INFO);
        hud.register(Welcomer.INFO);
        hud.register(OnTope.INFO);
        hud.register(CatGirl.INFO);
        hud.register(Logo.INFO);
        hud.register(PacketHud.INFO);
        hud.register(Radar.INFO);
        hud.register(ToastNotifications.INFO);
        hud.register(NotificationsHud.INFO);
        hud.register(PlayerRadarHud.INFO);
        hud.register(HealthESP.INFO);
        hud.register(NotificationsHudElement.INFO);
        hud.register(BindsHud.INFO);
        hud.register(TextPresets.INFO);
        hud.register(Presets.INFO);
        hud.register(TimerPlusCharge.INFO);
        hud.register(FriendHud.INFO);
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(ArkMode);
        Modules.registerCategory(atricombot);
        Modules.registerCategory(atrirender);
        Modules.registerCategory(atrimisc);
        Modules.registerCategory(Crash);
        Modules.registerCategory(SETTINGS);
    }

    @Override
    public String getPackage() {
        return "tcy.addon.atrihack";
    }
//    public static String getHWID() throws IOException {
//        String hwid = null;
//        try {
//            Process process = Runtime.getRuntime().exec("wmic csproduct get uuid");
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (!line.trim().isEmpty()) {
//                    hwid = line.trim();
//                }
//            }
//            reader.close();
//            process.waitFor();
//        } catch (IOException | InterruptedException e) {
//            e.printStackTrace();
//            throw new IOException("Failed to retrieve HWID.");
//        }
//        return hwid;
//
//    }
    public static void init() {
        String hwid;
//        try {
//            hwid = getHWID();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        if (!Hwid.validateHwid()) {
//            LOGGER.error("Unable to verify your hwid");
//            LOGGER.info(hwid);
//            System.exit(0);
//        }
    }
}
