package tcy.addon.atrihack.modules.player;

import meteordevelopment.meteorclient.events.game.SendMessageEvent;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import tcy.addon.atrihack.ATRIHack;
import tcy.addon.atrihack.ArkModule;

public class ChatSuffix extends ArkModule {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;

    public ChatSuffix() {
        super(ATRIHack.ArkMode, "Chat Suffix", "Suffix.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("Mode")).description("7t.exe")).defaultValue((Object)Mode.ATRI)).build());
    }

    private String getSuffix() {
        String suffix = null;
        switch ((Mode)(this.mode.get())) {
            case ATRI: {
                suffix = " | \uD835\uDC68\uD835\uDC7B\uD835\uDC79\uD835\uDC70 \uD835\uDC6F\uD835\uDC68\uD835\uDC6A\uD835\uDC72";
                break;
            }
            case MoonGod: {
                suffix = " 𝙼𝚘𝚘𝚗𝙶𝚘𝚍";
                break;
            }
            case StaRry_Client: {
                suffix = " StaRry";
                break;
            }
            case Melon: {
                suffix = " Ⲙⲉ𝓵ⲟⲛ";
                break;
            }
            case MelonBeta: {
                suffix = " \uD835\uDD10\uD835\uDD22\uD835\uDD29\uD835\uDD2C\uD835\uDD2B\uD835\uDD05\uD835\uDD22\uD835\uDD31\uD835\uDD1E";
                break;
            }
            case OnePlusOne: {
                suffix = " ☣1+1☣";
                break;
            }
            case Penis: {
                suffix = " ⲟ𝓵ⲟ";
            }
        }
        return suffix;
    }

    @EventHandler
    private void onMessageSend(SendMessageEvent event) {
        Object message = event.message;
        if (((String)message).startsWith(".") || ((String)message).startsWith("/")) {
            return;
        }
        event.message = (String) (message = (String)message + this.getSuffix());
    }

    public static enum Mode{
        ATRI,
        MoonGod,
        StaRry_Client,
        Melon,
        MelonBeta,

        OnePlusOne,
        Penis
    }
}
