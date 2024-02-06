package tcy.addon.atrihack.utils.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Stack;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FakeRiseGui extends GuiRender{
    private double x;
    private double y;
    private double buttonWidth =150;
    private double buttonHeight = 20;
    public static ArrayList<String> changeLog=new ArrayList<>();
    private final Identifier bg = new Identifier("atrihack", "bg.jpg");

    @Override
    public void draw(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        TextRenderer text=TextRenderer.get();
        text.begin(0.6);
        x=(this.width-buttonWidth)/2;
        y=this.height/2-25;
        final float xOffset = -1.0f * ((mouseX - this.width / 2.0f) / (this.width / 32.0f));
        final float yOffset = -1.0f * ((mouseY - this.height / 2.0f) / (this.height / 18.0f));
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        drawContext.drawTexture(bg, (int) (-16.0f + xOffset*1), (int) (-9.0f + yOffset*1), 0, 0, this.width + 32, this.height + 18, this.width + 32, this.height + 18);
        //RenderUtils.drawRiseShader(drawContext.getMatrices(),0,0,mc.getWindow().getWidth(),mc.getWindow().getHeight());

        //single
        if(!isMouseHoveringRect(x,y,buttonWidth,buttonHeight-2,mouseX,mouseY)) {
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 35), x, y, x + buttonWidth, y + buttonHeight - 1.5, 5, 4);
        }
        else{
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 75), x, y, x + buttonWidth, y + buttonHeight - 1.5, 5, 4);
        }
        text.render("Single",x+(buttonWidth-text.getWidth("Single"))/2 , y+6-1.5 ,new Color(255,255,255,240));

        //muilt
        if(!isMouseHoveringRect(x,y+buttonHeight,buttonWidth,buttonHeight-1,mouseX,mouseY)) {
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 35), x, y + buttonHeight, x + buttonWidth, y + 2 * buttonHeight - 1, 5, 4);
        }
        else{
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 75), x, y + buttonHeight, x + buttonWidth, y + 2 * buttonHeight - 1, 5, 4);
        }
        text.render("Muilt",x+(buttonWidth-text.getWidth("Muilt"))/2 , y+buttonHeight+6-1 ,new Color(255,255,255,240));

        //setting
        if(!isMouseHoveringRect(x,y+2*buttonHeight,buttonWidth/2-0.5,buttonHeight,mouseX,mouseY)) {
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 35), x, y + 2 * buttonHeight, x + buttonWidth / 2 - 0.5, y + 3 * buttonHeight, 5, 4);
        }
        else{
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 75), x, y + 2 * buttonHeight, x + buttonWidth / 2 - 0.5, y + 3 * buttonHeight, 5, 4);
        }
        text.render("Setting",x+(buttonWidth/2-text.getWidth("Setting"))/2-0.5 , y+2*buttonHeight+6 ,new Color(255,255,255,240));

        //quit
        if(!isMouseHoveringRect(x+buttonWidth/2+0.5,y+2*buttonHeight,buttonWidth/2-0.5,buttonHeight,mouseX,mouseY)) {
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 35), x + buttonWidth / 2 + 0.5, y + 2 * buttonHeight, x + buttonWidth, y + 3 * buttonHeight, 5, 4);
        }
        else{
            renderRoundedQuad(drawContext.getMatrices(), new java.awt.Color(255, 255, 255, 75), x + buttonWidth / 2 + 0.5, y + 2 * buttonHeight, x + buttonWidth, y + 3 * buttonHeight, 5, 4);
        }
        text.render("Quit",x+buttonWidth/2+(buttonWidth/2-text.getWidth("Quit"))/2+0.5 , y+2*buttonHeight+6 ,new Color(255,255,255,240));
        text.end();
        TextRenderer big=TextRenderer.get();
        big.begin(1.6);
        big.render("ATRIHack",x+(buttonWidth-big.getWidth("ATRIHack"))/2,y+6-1.5-big.getHeight(),new Color(255,255,255,240),false);
        big.end();

        TextRenderer log=TextRenderer.get();
        log.begin(0.4);
        log.render("Change Logs:",5,5,new Color(0,0,0,240),false);
        for(int i=1;i<=changeLog.size();i++){
            log.render(changeLog.get(i-1),5,5+i*(log.getHeight()+0.2),new Color(50,50,50,240),false);
        }
        log.end();
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isMouseHoveringRect(x, y, buttonWidth, buttonHeight - 2, mouseX, mouseY)) {
                mc.setScreen(new SelectWorldScreen(this));
            }
            if (isMouseHoveringRect(x, y + buttonHeight, buttonWidth, buttonHeight - 1, mouseX, mouseY)) {
                if (!mc.options.skipMultiplayerWarning) {
                    mc.options.skipMultiplayerWarning = true;
                    mc.options.write();
                }
                Screen screen = new MultiplayerScreen(this);
                mc.setScreen(screen);
            }
            if (isMouseHoveringRect(x, y + 2 * buttonHeight, buttonWidth / 2 - 0.5, buttonHeight, mouseX, mouseY)) {
                mc.setScreen(new OptionsScreen(this, mc.options));
            }
            if (isMouseHoveringRect(x + buttonWidth / 2 + 0.5, y + 2 * buttonHeight, buttonWidth / 2 - 0.5, buttonHeight, mouseX, mouseY)) {
                mc.stop();
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    public void RenderText(double scale, String str, double x, double y, Color color){
        TextRenderer text=TextRenderer.get();
        text.begin(scale);
        text.render(str,x,y,color,false);
        text.end();
    }
    public static void renderRoundedQuad(MatrixStack matrices, java.awt.Color c, double fromX, double fromY, double toX, double toY, double radC1, double radC2, double radC3, double radC4,
                                         double samples) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float f = transformColor((float) (color >> 24 & 255) / 255.0F);
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, radC1, radC2, radC3, radC4, samples);
        endRender();
    }
    public static void endRender() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
    public static float compute(int initialAlpha) {
        float alpha = initialAlpha;
        for (Float alphaMultiplier : alphaMultipliers) {
            alpha *= alphaMultiplier;
        }
        return alpha;
    }
    public static float transformColor(float f) {
        return compute((int) (f * 255)) / 255f;
    }
    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
    private static final Stack<Float> alphaMultipliers = new Stack<>();

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radC1, double radC2,
                                                 double radC3, double radC4, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] map = new double[][] { new double[] { toX - radC4, toY - radC4, radC4 }, new double[] { toX - radC2, fromY + radC2, radC2 },
            new double[] { fromX + radC1, fromY + radC1, radC1 }, new double[] { fromX + radC3, toY - radC3, radC3 } };
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r < 360 / 4d + i * 90d; r += 90 / samples) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            }
            float rad1 = (float) Math.toRadians(360 / 4d + i * 90d);
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void renderRoundedQuad(MatrixStack stack, java.awt.Color c, double x, double y, double x1, double y1, double rad, double samples) {
        renderRoundedQuad(stack, c, x, y, x1, y1, rad, rad, rad, rad, samples);
    }
}
