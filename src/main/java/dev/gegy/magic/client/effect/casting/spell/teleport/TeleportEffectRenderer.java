package dev.gegy.magic.client.effect.casting.spell.teleport;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

public final class TeleportEffectRenderer {
    public void render(final Minecraft client, final WorldRenderContext context, final TeleportEffect effect) {
        final Font textRenderer = client.font;

        final long worldTime = context.world().getGameTime();
        final float tickDelta = context.tickDelta();

        final Vec3 sourcePos = effect.source().getPosition(tickDelta);
        final Vec3 cameraPos = context.camera().getPosition();

        final float time = (float) (worldTime - effect.createTime()) + tickDelta;

        final TeleportTargetAnimator animator = effect.animator();
        final List<TeleportEffect.Symbol> targets = effect.symbols();

        final PoseStack matrixStack = context.matrixStack();
        matrixStack.pushPose();
        matrixStack.translate(
                (float) (sourcePos.x - cameraPos.x),
                (float) (sourcePos.y - cameraPos.y),
                (float) (sourcePos.z - cameraPos.z)
        );
        matrixStack.mulPoseMatrix(effect.sourcePlane().planeToWorld());

        final float scale = 0.0625f * TeleportEffect.SYMBOL_SIZE;
        matrixStack.scale(-scale, -scale, scale);

        for (int index = 0; index < targets.size(); index++) {
            final TeleportEffect.Symbol target = targets.get(index);
            final Vec2 position = animator.getPosition(index, time);
            final float opacity = animator.getOpacity(index, time);
            if (opacity <= 0.0f) {
                continue;
            }

            final Matrix4f matrix = matrixStack.last().pose();

            final float x = (position.x / scale) + target.offsetX();
            final float y = (position.y / scale) + target.offsetY();

            final int alpha = Mth.floor(opacity * 255.0f) << 24;
            final int innerColor = target.innerColor().packed() | alpha;
            final int outlineColor = target.outlineColor().packed() | alpha;

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
