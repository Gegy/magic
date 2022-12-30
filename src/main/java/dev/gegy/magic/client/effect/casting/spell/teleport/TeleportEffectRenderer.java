package dev.gegy.magic.client.effect.casting.spell.teleport;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;

public final class TeleportEffectRenderer {
    public void render(Minecraft client, WorldRenderContext context, TeleportEffect effect) {
        var textRenderer = client.font;

        long worldTime = context.world().getGameTime();
        float tickDelta = context.tickDelta();

        var sourcePos = effect.source().getPosition(tickDelta);
        var cameraPos = context.camera().getPosition();

        float time = (float) (worldTime - effect.createTime()) + tickDelta;

        var animator = effect.animator();
        var targets = effect.symbols();

        var matrixStack = context.matrixStack();
        matrixStack.pushPose();
        matrixStack.translate(
                (float) (sourcePos.x - cameraPos.x),
                (float) (sourcePos.y - cameraPos.y),
                (float) (sourcePos.z - cameraPos.z)
        );
        matrixStack.mulPoseMatrix(effect.sourcePlane().planeToWorld());

        float scale = 0.0625F * TeleportEffect.SYMBOL_SIZE;
        matrixStack.scale(-scale, -scale, scale);

        for (int index = 0; index < targets.size(); index++) {
            var target = targets.get(index);
            var position = animator.getPosition(index, time);
            float opacity = animator.getOpacity(index, time);
            if (opacity <= 0.0F) {
                continue;
            }

            var matrix = matrixStack.last().pose();

            float x = (position.x / scale) + target.offsetX();
            float y = (position.y / scale) + target.offsetY();

            int alpha = Mth.floor(opacity * 255.0F) << 24;
            int innerColor = target.innerColor().packed() | alpha;
            int outlineColor = target.outlineColor().packed() | alpha;

            textRenderer.drawInBatch8xOutline(
                    target.text(),
                    x, y,
                    innerColor, outlineColor,
                    matrix, context.consumers(),
                    LightTexture.FULL_BRIGHT
            );
        }

        matrixStack.popPose();
    }
}
