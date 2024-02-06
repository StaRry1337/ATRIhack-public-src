package tcy.addon.atrihack.hud;


import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.elements.TextHud;
import tcy.addon.atrihack.ATRIHack;

public class TextPresets {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(ATRIHack.HUD_GROUP, "banana-text", "Displays arbitrary text with Starscript.", TextPresets::create);

    static {
        addPreset("Kills", "Kills: #1{atrihack.kills}", 0);
        addPreset("Deaths", "Deaths: #1{atrihack.deaths}", 0);
        addPreset("KDR", "KDR: #1{atrihack.kdr}", 0);
        addPreset("Highscore", "Highscore: #1{atrihack.highscore}", 0);
        addPreset("Killstreak", "Killstreak: #1{atrihack.killstreak}", 0);
        addPreset("Crystals/s", "Crystals/s: #1{atrihack.crystalsps}", 0);
        addPreset("Anchor/s", "Anchor/s: #1{atrihack.anchorps}", 0);

    }

    private static TextHud create() {
        return new TextHud(INFO);
    }

    private static HudElementInfo<TextHud>.Preset addPreset(String title, String text, int updateDelay) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
            if (updateDelay != -1) textHud.updateDelay.set(updateDelay);
        });
    }
}
