//package tcy.addon.atrihack.hud;
//
//import com.mojang.blaze3d.systems.RenderSystem;
//import meteordevelopment.meteorclient.settings.*;
//import meteordevelopment.meteorclient.utils.render.color.SettingColor;
//import meteordevelopment.orbit.EventHandler;
//import net.minecraft.client.gui.DrawContext;
//import net.minecraft.client.render.*;
//import tcy.addon.atrihack.ATRIHack;
//import tcy.addon.atrihack.ArkModule;
//
//import tcy.addon.atrihack.modules.render.MineESP;
//import tcy.addon.atrihack.utils.RenderUtils;
//
//
//import java.awt.*;
//
//import static tcy.addon.atrihack.utils.RenderUtils.endRender;
//import static tcy.addon.atrihack.utils.RenderUtils.setupRender;
//
//public class Crosshair extends ArkModule {
//
//    public Crosshair() {
//        super(ATRIHack.atrirender,"Crosshair",".HUD");
//    }
//    private  final SettingGroup sgRender = settings.createGroup("Render");
//    private final Setting<Modess> mode = sgRender.add(new EnumSetting.Builder<Modess>()
//        .name("Mode")
//        .description("The mode to render in.")
//        .defaultValue(Modess.Sync)
//        .build()
//    );
//    private  final Setting<ColorMode> colorMode = sgRender.add(new EnumSetting.Builder<ColorMode>()
//        .name("ColorMode")
//        .description("The mode to render in.")
//        .defaultValue(ColorMode.Sky)
//        .build()
//    );
//    private final Setting<Integer> colorSpeed = sgRender.add(new IntSetting.Builder()
//        .name("colorSpeed")
//        .description("")
//        .defaultValue(2)
//        .range(1, 20)
//        .sliderRange(1, 20)
//        .build()
//    );
//    private final Setting<SettingColor> colorww = sgRender.add(new ColorSetting.Builder()
//        .name("colorww")
//        .description("")
//        .defaultValue(new SettingColor(255,255,255,100))
//        .build()
//    );
//    private final Setting<SettingColor> color = sgRender.add(new ColorSetting.Builder()
//        .name("color")
//        .description("")
//        .defaultValue(new SettingColor(0x2250b4b4))
//        .build()
//    );
//    private final Setting<SettingColor> hcolor1 = sgRender.add(new ColorSetting.Builder()
//        .name("hcolor1")
//        .description("")
//        .defaultValue(new SettingColor(-6974059))
//        .build()
//    );
//    private final Setting<SettingColor> acolor = sgRender.add(new ColorSetting.Builder()
//        .name("acolor")
//        .description("")
//        .defaultValue(new SettingColor(-6974059))
//        .build()
//    );
//
//    private enum Modess {
//        Custom, Sync
//    }
//
//    public  Color getColor(int count) {
//        int index = count;
//        switch (colorMode.get()) {
//            case Sky -> {
//                return RenderUtils.skyRainbow(colorSpeed.get(), index);
//            }
//            case LightRainbow -> {
//                return RenderUtils.rainbow((int) colorSpeed.get(), index, .6f, 1, 1);
//            }
//            case Rainbow -> {
//                return RenderUtils.rainbow((int) colorSpeed.get(), index, 1f, 1, 1);
//            }
//            default -> {
//                return null;
//            }
//        }
//    }
//
//    public  void drawElipseSync(float x, float y, float rx, float ry, float start, float end, float radius, SettingColor color) {
//
//        if (start > end) {
//            float endOffset = end;
//            end = start;
//            start = endOffset;
//        }
//
//        RenderSystem.enableBlend();
//        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferBuilder = tessellator.getBuffer();
//        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
//        RenderSystem.lineWidth(100F);
//
//        setupRender();
//        for (float i = start; i <= end; i += 4) {
//            float cos = (float) Math.cos(i * Math.PI / 180) * (radius / ry);
//            float sin = (float) Math.sin(i * Math.PI / 180) * (radius / rx);
//            bufferBuilder.vertex((x + cos), (y + sin), 0f).color(colorww.get().r,colorww.get().g,colorww.get().b,colorww.get().a).next();
//        }
//        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
//        RenderSystem.lineWidth(1F);
//        RenderSystem.disableBlend();
//        endRender();
//    }
//    public void onRender2D(DrawContext context) {
//        if (!mc.options.getPerspective().isFirstPerson()) return;
//
//
//        int progress = (int) (360 * (mc.player.handSwingProgress));
//        progress = progress == 0 ? 360 : progress;
//
//
//        RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 0, 360, 4f, new SettingColor(0,0,0,255));
//        RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 0, 360, 3.5f, new SettingColor(0,0,0,255));
//
//        if (mode.get() == Modess.Custom) {
//            RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 4f, color.get());
//            RenderUtils.drawElipse(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 3.5f, color.get());
//        } else {
//            drawElipseSync(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 4f, color.get());
//            drawElipseSync(mc.getWindow().getScaledWidth() / 2f, mc.getWindow().getScaledHeight() / 2f, 1f, 1f, 270, progress + 270, 3.5f, color.get());
//        }
//    }
//    public enum ColorMode {
//        Sky,
//        LightRainbow,
//        Rainbow
//    }
//}
