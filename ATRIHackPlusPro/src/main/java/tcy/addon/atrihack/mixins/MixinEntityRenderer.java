package tcy.addon.atrihack.mixins;
import tcy.addon.atrihack.modules.render.SimpleNameTag;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.player.PlayerEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer<T extends Entity> {
    private EntityRenderDispatcher dispatcher;
    private TextRenderer textRenderer;

    private void EntityRenderer(EntityRendererFactory.Context ctx) {
        this.dispatcher = ctx.getRenderDispatcher();
        this.textRenderer = ctx.getTextRenderer();
    }
    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void renderLabelIfPresent(T entity, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        Text finalt = Text.of(SimpleNameTag.finalt);
        SimpleNameTag n= Modules.get().get(SimpleNameTag.class);
        if (entity instanceof PlayerEntity && n.isActive()) {
            info.cancel();
            double d = this.dispatcher.getSquaredDistanceToCamera(entity);
            if (!(d > 4096.0)) {
                boolean bl = !entity.isSneaky();
                float f = entity.getNameLabelHeight();
                int i = "deadmau5".equals(finalt.getString()) ? -10 : 0;
                matrices.push();
                matrices.translate(0.0F, f, 0.0F);
                matrices.multiply(this.dispatcher.getRotation());
                matrices.scale(-0.025F, -0.025F, 0.025F);
                Matrix4f matrix4f = matrices.peek().getPositionMatrix();
                float g = MinecraftClient.getInstance().options.getTextBackgroundOpacity(0.25F);
                int j = (int)(g * 255.0F) << 24;
                TextRenderer textRenderer = this.getTextRenderer();
                float h = (float)(-textRenderer.getWidth(finalt) / 2);
                textRenderer.draw(finalt, h, (float)i, 553648127, false, matrix4f, vertexConsumers, bl ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, j, light);
                if (bl) {
                    textRenderer.draw(finalt, h, (float)i, -1, false, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
                }
                matrices.pop();
            }

            //info.cancel();
        }
    }
}
