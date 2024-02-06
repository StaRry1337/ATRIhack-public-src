package tcy.addon.atrihack.modules.render;


import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static tcy.addon.atrihack.modules.Notifications.*;
public class SimpleNameTag extends ArkModule {
    public final Object2IntMap<UUID> totemPopMap = new Object2IntOpenHashMap<>();

    public static String finalt = "";
    public SimpleNameTag() {
        super(ATRIHack.atrirender, "SimpleNameTag", "SimpleNameTag.");
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private String getHealthColor(PlayerEntity entity) {
        int health = (int) ((int) entity.getHealth() + entity.getAbsorptionAmount());
        if (health <= 22 && health >= 10) return Formatting.YELLOW + "";
        if (health > 22) return Formatting.GREEN + "";
        return Formatting.RED + "";
    }
    public final Setting<Boolean> ping = sgGeneral.add(new BoolSetting.Builder()
        .name("Ping")
        .description("Boolean")
        .defaultValue(true)
        .build()
    );
    public final Setting<Boolean> pop = sgGeneral.add(new BoolSetting.Builder()
        .name("POPs")
        .description("Boolean")
        .defaultValue(true)
        .build()
    );

    @EventHandler
    public void onupdate(Render3DEvent e) {
        finalt="";
        for (Entity ent : mc.world.getPlayers()) {
            if (ent == mc.player) continue;
            PlayerEntity p = (PlayerEntity) ent;
            if (ping.get()) {
                finalt += Formatting.GREEN+String.valueOf(getEntityPing(p))+"ms ";
            }
            finalt += Formatting.WHITE+String.valueOf( mc.world.getEntityById(ent.getId()).getName()) + " ";
            finalt += getHealthColor(p) + Math.floor(p.getHealth()) + " ";
            if (pop.get()) {
                synchronized (totemPopMap) {
                    if (totemPopMap.containsKey(p.getUuid())) {
                        int pops = totemPopMap.getOrDefault(p.getUuid(), 0);
                        finalt += Formatting.DARK_PURPLE+"Pops "+Formatting.WHITE+String.valueOf(pops);
                    } else {
                        finalt += Formatting.DARK_PURPLE+"Pops "+Formatting.WHITE+"0";
                    }
                }
            }
            finalt=removeBracesAndLiteral(finalt);

        }

    }
    public static String removeBracesAndLiteral(String input) {
        Pattern pattern = Pattern.compile("[{}]|literal");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }
    public int getEntityPing(PlayerEntity entity) {
        if (mc.getNetworkHandler() == null) return 0;
        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(entity.getUuid());
        if (playerListEntry == null) return 0;
        return playerListEntry.getLatency();
    }
}
