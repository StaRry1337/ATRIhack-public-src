package tcy.addon.atrihack.utils;

import com.ibm.icu.impl.number.DecimalQuantity;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.util.math.*;
import org.joml.Matrix4f;
import org.joml.Quaterniond;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL46;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static org.spongepowered.asm.util.JavaVersion.current;


/**
 * @author OLEPOSSU
 */

public class RenderUtils {
    private static final VertexConsumerProvider.Immediate vertex = VertexConsumerProvider.immediate(new BufferBuilder(2048));

    private final MinecraftClient client;
    private final TextRenderer textRenderer;
    private int color;
    private int globalOffset = 0;
    private boolean cycle = false;

    public RenderUtils(MinecraftClient client) {
        this.client = client;
        this.textRenderer = client.textRenderer;
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
    public static void endRender() {
        RenderSystem.disableBlend();
    }
    public Color getColorObject() {
        int color = getColorw();
        int alpha = (color >> 24) & 0xff;
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = (color) & 0xFF;

        return new Color(red, green, blue, alpha);

    }
    /*
    public static void drawCircle(MatrixStack matrices ,Float x, Float y, Float radius , ColorRGB color) {
        drawRound(matrices, x - radius, y - radius, radius * 2, radius * 2, radius, color);
    }

    public static void  drawRound(MatrixStack matrices, Float x, Float y, Float width, Float height, Float radius, ColorRGB color) {
        renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius.doubleValue(), 9.0);
    }

    public static void  renderRoundedQuad(MatrixStack matrices, ColorRGB c, Float fromX, Float fromY, Float toX, Float toY, Double radius, Double samples) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        setupRender();
        RenderSystem.setShader ((Supplier<ShaderProgram>) GameRenderer.getPositionColorProgram());
        renderRoundedQuadInternal(matrix, c.rFloat, c.gFloat, c.bFloat, c.aFloat, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }

    public static void  renderRoundedQuadInternal(
        Matrix4f matrix,
        Float cr,
        Float cg,
        Float cb,
        Float ca,
        Float fromX,
        Float fromY,
        Float toX,
        Float toY,
        Double radius,
        Double samples
    ) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        val map = arrayOf(
            doubleArrayOf(toX - radius, toY - radius, radius),
            doubleArrayOf(toX - radius, fromY + radius, radius),
            doubleArrayOf(fromX + radius, fromY + radius, radius),
            doubleArrayOf(fromX + radius, toY - radius, radius)
        )
        for (i in 0..3){
            double[] current = map[i];
            double rad = current[2];
            double r = i * 90.0;
            Object sin;
            double rad1;
            Object cos;
            while (r < 360 / 4.0 + i * 90.0) {
                rad1 = Math.toRadians(r);
                sin = (sin(rad1.toDouble()) * rad).toFloat();
                cos = (cos(rad1.toDouble()) * rad).toFloat();
                bufferBuilder.vertex(matrix, current[0].toFloat() + sin, current[1].toFloat() + cos, 0.0f)
                    .color(cr, cg, cb, ca).next();
                r += 90 / samples;
            }
            rad1 = Math.toRadians(360 / 4.0 + i * 90.0).toFloat();
            sin = (sin(rad1.toDouble()) * rad).toFloat();
            cos = (cos(rad1.toDouble()) * rad).toFloat();
            bufferBuilder.vertex(matrix, current[0].toFloat() + sin, current[1].toFloat() + cos, 0.0f)
                .color(cr, cg, cb, ca).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

     */
    public int getColorw() {
        if (cycle) {
            float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            double rainbowState = Math.ceil((System.currentTimeMillis() + 300 + globalOffset) / 20.0);
            rainbowState %= 360;
            int rgb = Color.getHSBColor((float) (rainbowState / 360.0f), hsb[1], hsb[2]).getRGB();
            int alpha = (color >> 24) & 0xff;
            int red = (rgb >> 16) & 0xFF;
            int green = (rgb >> 8) & 0xFF;
            int blue = (rgb) & 0xFF;
            return ((alpha & 0xFF) << 24) |
                ((red & 0xFF) << 16) |
                ((green & 0xFF) << 8) |
                ((blue & 0xFF));
        }
        return color;
    }
    public static ByteBuffer readImageToBuffer(InputStream in) throws IOException {
        BufferedImage bufferedimage = ImageIO.read(in);
        int[] pixelIndex = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
        ByteBuffer bytebuffer = ByteBuffer.allocate(4 * pixelIndex.length);
        Arrays.stream(pixelIndex).map(i -> i << 8 | i >> 24 & 0xFF).forEach(bytebuffer::putInt);
        bytebuffer.flip();
        return bytebuffer;
    }

    public static void drawElipse(float x, float y, float rx, float ry, float start, float end, float radius, SettingColor color) {

        if (start > end) {
            float endOffset = end;
            end = start;
            start = endOffset;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        RenderSystem.lineWidth(100F);

        setupRender();
        for (float i = start; i <= end; i += 4) {
            float cos = (float) Math.cos(i * Math.PI / 180) * (radius / ry);
            float sin = (float) Math.sin(i * Math.PI / 180) * (radius / rx);
            bufferBuilder.vertex((x + cos), (y + sin), 0f).color(color.copy().r, color.copy().g, color.copy().b, color.copy().a).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.lineWidth(1F);
        RenderSystem.disableBlend();
        endRender();
    }
    public static Color skyRainbow(int speed, int index) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        return Color.getHSBColor((double) ((float) ((angle %= 360.0) / 360.0)) < 0.5 ? -((float) (angle / 360.0))
            : (float) (angle / 360.0), 0.5F, 1.0F);
    }
    public static Color rainbow(int speed, int index, float saturation, float brightness, float opacity) {
        int angle = (int) ((System.currentTimeMillis() / speed + index) % 360);
        float hue = angle / 360f;
        Color color = new Color(Color.HSBtoRGB(hue, saturation, brightness));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(),
            Math.max(0, Math.min(255, (int) (opacity * 255))));
    }
    public static int fade(int startColor, int endColor, float progress) {
        float invert = 1.0f - progress;
        int r = (int) ((startColor >> 16 & 0xFF) * invert + (endColor >> 16 & 0xFF) * progress);
        int g = (int) ((startColor >> 8 & 0xFF) * invert + (endColor >> 8 & 0xFF) * progress);
        int b = (int) ((startColor & 0xFF) * invert + (endColor & 0xFF) * progress);
        int a = (int) ((startColor >> 24 & 0xFF) * invert + (endColor >> 24 & 0xFF) * progress);
        return (a & 0xFF) << 24 | (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }
    public static Color TwoColoreffect(Color cl1, Color cl2, double speed) {
        double thing = speed / 4.0 % 1.0;
        float val = MathHelper.clamp((float) Math.sin(Math.PI * 6 * thing) / 2.0f + 0.5f, 0.0f, 1.0f);
        return new Color(lerp((float) cl1.getRed() / 255.0f, (float) cl2.getRed() / 255.0f, val), lerp((float) cl1.getGreen() / 255.0f, (float) cl2.getGreen() / 255.0f, val), lerp((float) cl1.getBlue() / 255.0f, (float) cl2.getBlue() / 255.0f, val));
    }
    public static float lerp(float a, float b, float f) {
        return a + f * (b - a);
    }
    public static Color getAnalogousColor(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        float degree = 0.84f;
        float newHueSubtracted = hsb[0] - degree;
        return new Color(Color.HSBtoRGB(newHueSubtracted, hsb[1], hsb[2]));
    }
    public static Color interpolateColorsBackAndForth(int speed, int index, Color start, Color end, boolean trueColor) {
        int angle = (int) (((System.currentTimeMillis()) / speed + index) % 360);
        angle = (angle >= 180 ? 360 - angle : angle) * 2;
        return trueColor ? interpolateColorHue(start, end, angle / 360f)
            : interpolateColorC(start, end, angle / 360f);
    }
    public static Color interpolateColorHue(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));

        float[] color1HSB = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] color2HSB = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        Color resultColor = Color.getHSBColor(interpolateFloat(color1HSB[0], color2HSB[0], amount),
            interpolateFloat(color1HSB[1], color2HSB[1], amount),
            interpolateFloat(color1HSB[2], color2HSB[2], amount));

        return new Color(resultColor.getRed(), resultColor.getGreen(), resultColor.getBlue(),
            interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount),
            interpolateInt(color1.getGreen(), color2.getGreen(), amount),
            interpolateInt(color1.getBlue(), color2.getBlue(), amount),
            interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }
    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int) interpolate(oldValue, newValue, (float) interpolationValue);
    }
    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }
    public static void draw3DBox(MatrixStack matrixStack, Box box, int color, float alpha) {
        RenderSystem.setShaderColor(ColorHelper.Argb.getRed(color),ColorHelper.Argb.getGreen(color) , ColorHelper.Argb.getBlue(color), 1.0f);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        Tessellator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();

        tessellator.draw();

        RenderSystem.setShaderColor(ColorHelper.Argb.getRed(color),ColorHelper.Argb.getGreen(color) , ColorHelper.Argb.getBlue(color), alpha);
        RenderSystem.setShader(GameRenderer::getPositionProgram);

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();

        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();

        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).next();
        bufferBuilder.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).next();
        tessellator.draw();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
    }
    public static void rounded(MatrixStack stack, float x, float y, float w, float h, float radius, int p, int color) {

        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        corner(x + w, y, radius, 360, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y, radius, 270, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x, y + h, radius, 180, p, r, g, b, a, bufferBuilder, matrix4f);
        corner(x + w, y + h, radius, 90, p, r, g, b, a, bufferBuilder, matrix4f);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }


    public static void corner(float x, float y, float radius, int angle, float p, float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix4f) {
        for (float i = angle; i > angle - 90; i -= 90 / p) {
            bufferBuilder.vertex(matrix4f, (float) (x + Math.cos(Math.toRadians(i)) * radius), (float) (y + Math.sin(Math.toRadians(i)) * radius), 0).color(r, g, b, a).next();
        }
    }

    public static void text(String text, MatrixStack stack, float x, float y, int color) {
        mc.textRenderer.draw(text, x, y, color, false, stack.peek().getPositionMatrix(), vertex, TextRenderer.TextLayerType.NORMAL, 0, 15728880);
        vertex.draw();
    }

    public static void quad(MatrixStack stack, float x, float y, float w, float h, int color) {
        Matrix4f matrix4f = stack.peek().getPositionMatrix();

        float a = (float) ColorHelper.Argb.getAlpha(color) / 255.0F;
        float r = (float) ColorHelper.Argb.getRed(color) / 255.0F;
        float g = (float) ColorHelper.Argb.getGreen(color) / 255.0F;
        float b = (float) ColorHelper.Argb.getBlue(color) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix4f, x + w, y, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x, y, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x, y + h, 0).color(r, g, b, a).next();
        bufferBuilder.vertex(matrix4f, x + w, y + h, 0).color(r, g, b, a).next();


        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }
}
