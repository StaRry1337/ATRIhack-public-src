package tcy.addon.atrihack.hud;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import tcy.addon.atrihack.ATRIHack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FriendHud extends HudElement {
    public static final HudElementInfo<FriendHud> INFO = new HudElementInfo<>(ATRIHack.HUD_BLACKOUT, "FriendHud", "", FriendHud::new);
    public FriendHud() {
        super(INFO);
    }
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgBackground = settings.createGroup("Background");
    private final SettingGroup sgScale = settings.createGroup("Scale");

    private final Setting<Integer> border = sgGeneral.add(new IntSetting.Builder()
        .name("border")
        .description("How much space to add around the element.")
        .defaultValue(0)
        .build()
    );
    private final Setting<Integer> limit = sgGeneral.add(new IntSetting.Builder()
        .name("limit")
        .description("The max number of players to show.")
        .defaultValue(10)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );
    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Renders shadow behind text.")
        .defaultValue(true)
        .build()
    );
    private final Setting<Alignment> alignment = sgGeneral.add(new EnumSetting.Builder<Alignment>()
        .name("alignment")
        .description("Horizontal alignment.")
        .defaultValue(Alignment.Auto)
        .build()
    );
    private final Setting<SettingColor> primaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("primary-color")
        .description("Primary color.")
        .defaultValue(new SettingColor())
        .build()
    );
    private final Setting<SettingColor> secondaryColor = sgGeneral.add(new ColorSetting.Builder()
        .name("secondary-color")
        .description("Secondary color.")
        .defaultValue(new SettingColor(175, 175, 175))
        .build()
    );
    private final Setting<Boolean> background = sgBackground.add(new BoolSetting.Builder()
        .name("background")
        .description("Displays background.")
        .defaultValue(false)
        .build()
    );
    private final Setting<SettingColor> backgroundColor = sgBackground.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color used for the background.")
        .visible(background::get)
        .defaultValue(new SettingColor(25, 25, 25, 50))
        .build()
    );
    private final Setting<Boolean> customScale = sgScale.add(new BoolSetting.Builder()
        .name("custom-scale")
        .description("Applies custom text scale rather than the global one.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> scale = sgScale.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Custom scale.")
        .visible(customScale::get)
        .defaultValue(1)
        .min(0.5)
        .sliderRange(0.5, 3)
        .build()
    );
    private final List<AbstractClientPlayerEntity> players = new ArrayList<>();

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + border.get() * 2, height + border.get() * 2);
    }

    @Override
    protected double alignX(double width, Alignment alignment) {
        return box.alignX(getWidth() - border.get() * 2, width, alignment);
    }

    @Override
    public void tick(HudRenderer renderer) {
        double width = renderer.textWidth("->", shadow.get(), getScale());
        double height = renderer.textHeight(shadow.get(), getScale());

        if (mc.world == null) {
            setSize(width, height);
            return;
        }
        for (PlayerEntity entity : getPlayers()) {
            if (entity.equals(mc.player)) continue;
            if (!Friends.get().isFriend(entity)) continue;

        }
        setSize(width, height);
    }
    @Override
    public void render(HudRenderer renderer) {
        double y = this.y + border.get();
        if (background.get()) {
            renderer.quad(this.x, this.y, getWidth(), getHeight(), backgroundColor.get());
        }
        renderer.text("Friends:", x + border.get() + alignX(renderer.textWidth("Friends:", shadow.get(), getScale()), alignment.get()), y, secondaryColor.get(), shadow.get(), getScale());

        if (mc.world == null) return;
        double spaceWidth = renderer.textWidth(" ", shadow.get(), getScale());
        for (PlayerEntity entity : getPlayers()) {
            if (!Friends.get().isFriend(entity)) continue;
            String text =  "- "  + entity.getEntityName();
            Color color = PlayerUtils.getPlayerColor(entity, primaryColor.get());
            double width = renderer.textWidth(text, shadow.get(), getScale());

            double x = this.x + border.get() + alignX(width, alignment.get());
            y += renderer.textHeight(shadow.get(), getScale()) + 2;
            x = renderer.text(text, x, y, color, shadow.get());
            renderer.text(text, x + spaceWidth, y, secondaryColor.get(), shadow.get(), getScale());
        }
    }
    private List<AbstractClientPlayerEntity> getPlayers() {
        players.clear();
        players.addAll(mc.world.getPlayers());
        if (players.size() > limit.get()) players.subList(limit.get() - 1, players.size() - 1).clear();
        players.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(mc.getCameraEntity())));

        return players;
    }
    private double getScale() {
        return customScale.get() ? scale.get() : -1;
    }
}
