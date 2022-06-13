package dev.gegy.magic.client.effect.casting.spell.teleport;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public final class TeleportEffectSystem implements EffectSystem {
    private static final float SELECT_THRESHOLD = 1.2F * TeleportEffect.SYMBOL_SIZE;

    private final TeleportEffectRenderer renderer = new TeleportEffectRenderer();

    public static TeleportEffectSystem create(ResourceManager resources) throws IOException {
        return new TeleportEffectSystem();
    }

    @Override
    public void render(MinecraftClient client, WorldRenderContext context, Framebuffer targetFramebuffer, EffectSelector effects) {
        RenderSystem.disableCull();

        for (var effect : effects.select(TeleportEffect.TYPE)) {
            this.renderer.render(client, context, effect);
        }

        RenderSystem.enableCull();
    }

    @Override
    public void tick(MinecraftClient client, EffectSelector effects) {
        long worldTime = client.world.getTime();

        for (var effect : effects.select(TeleportEffect.TYPE)) {
            int targetIndex = this.selectTarget(effect, worldTime);
        }
    }

    private int selectTarget(TeleportEffect effect, long worldTime) {
        var pointer = this.computePointer(effect.source(), effect.sourcePlane());
        if (pointer != null) {
            float time = (float) (worldTime - effect.createTime());
            return effect.animator().selectWithin(pointer, time, SELECT_THRESHOLD);
        } else {
            return -1;
        }
    }

    @Nullable
    private Vec2f computePointer(SpellSource source, GlyphPlane plane) {
        var direction = new Vec3f(source.getLookVector(1.0F));
        var intersection = plane.raycast(Vec3f.ZERO, direction);
        if (intersection != null) {
            return new Vec2f(intersection.getX(), intersection.getY());
        } else {
            return null;
        }
    }

    @Override
    public void close() {
    }
}
