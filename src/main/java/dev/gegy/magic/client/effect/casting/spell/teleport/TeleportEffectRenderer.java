package dev.gegy.magic.client.effect.casting.spell.teleport;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.MathHelper;

public final class TeleportEffectRenderer {
    public void render(MinecraftClient client, WorldRenderContext context, TeleportEffect effect) {
        var textRenderer = client.textRenderer;

        long worldTime = context.world().getTime();
        float tickDelta = context.tickDelta();

        var animator = effect.animator();
        var targets = effect.symbols();

        var sourcePos = effect.source().getPosition(tickDelta);
        var cameraPos = context.camera().getPos();

        float time = (float) (worldTime - effect.createTime()) + tickDelta;

        var matrixStack = context.matrixStack();
        matrixStack.push();
        matrixStack.translate(
                (float) (sourcePos.x - cameraPos.x),
                (float) (sourcePos.y - cameraPos.y),
                (float) (sourcePos.z - cameraPos.z)
        );
        matrixStack.multiplyPositionMatrix(effect.sourcePlane().getPlaneToWorldMatrix());

        float scale = 0.0625F * TeleportEffect.SYMBOL_SIZE;
        matrixStack.scale(-scale, -scale, scale);

        for (int index = 0; index < targets.size(); index++) {
            var target = targets.get(index);
            var position = animator.getPosition(index, time);
            float opacity = animator.getOpacity(index, time);
            if (opacity <= 0.0F) {
                continue;
            }

            var matrix = matrixStack.peek().getPositionMatrix();

            float x = (position.x / scale) + target.offsetX();
            float y = (position.y / scale) + target.offsetY();

            int alpha = MathHelper.floor(opacity * 255.0F) << 24;
            int innerColor = target.innerColor().packed() | alpha;
            int outlineColor = target.outlineColor().packed() | alpha;

            textRenderer.drawWithOutline(
                    target.text(),
                    x, y,
                    innerColor, outlineColor,
                    matrix, context.consumers(),
                    LightmapTextureManager.MAX_LIGHT_COORDINATE
            );
        }

        matrixStack.pop();
    }
}
