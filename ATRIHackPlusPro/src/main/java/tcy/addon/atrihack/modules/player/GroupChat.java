package tcy.addon.atrihack.modules.player;


import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringListSetting;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.PlayerListEntry;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;

import java.util.List;

public class GroupChat extends ArkModule {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // General

    private final Setting<List<String>> players = sgGeneral.add(new StringListSetting.Builder()
        .name("players")
        .description("Determines which players to message.")
        .defaultValue(
            "Two_C1_Yuan"
        )
        .build()
    );

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("How the message command is set up on the server.")
        .defaultValue("/msg %player% %message%")
        .build()
    );

    public GroupChat() {
        super(ATRIHack.ArkMode, "Group Chat", "Talks with people in groups privately using /msg.");
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        for(String playerString: players.get()) {
            for(PlayerListEntry onlinePlayer: mc.getNetworkHandler().getPlayerList()) {
                if (onlinePlayer.getProfile().getName().equalsIgnoreCase(playerString)) {
                    ChatUtils.sendPlayerMsg(command.get().replace("%player%", onlinePlayer.getProfile().getName()).replace("%message%", event.message));
                    break;
                }
            }
        }

        event.cancel();
    }
}
