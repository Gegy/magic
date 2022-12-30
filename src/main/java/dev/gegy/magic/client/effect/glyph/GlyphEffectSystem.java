package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public final class GlyphEffectSystem implements EffectSystem {
    private final GlyphEffectRenderer renderer;
    private final Frame frame;

    private final GlyphRenderParameters parameters = new GlyphRenderParameters();

    private GlyphEffectSystem(final GlyphEffectRenderer renderer) {
        this.renderer = renderer;
        frame = new Frame(renderer);
    }

    public static GlyphEffectSystem create(final ResourceManager resources) throws IOException {
        final GlyphEffectRenderer renderer = GlyphEffectRenderer.create(resources);
        return new GlyphEffectSystem(renderer);
    }

    @Override
    public void render(final Minecraft client, final WorldRenderContext context, final RenderTarget targetFramebuffer, final EffectSelector effects) {
        // TODO: frustum culling

        try (final Frame frame = this.frame.setup(targetFramebuffer)) {
            final GlyphRenderParameters parameters = this.parameters;
            for (final GlyphsEffect effect : effects.select(GlyphsEffect.TYPE)) {
                effect.render(parameters, context, frame);
            }
        }
    }

    @Override
    public void close() {
        renderer.close();
    }

    private static final class Frame implements GlyphsEffect.RenderFunction, AutoCloseable {
        private final GlyphEffectRenderer renderer;

        private RenderTarget target;
        private GlyphEffectRenderer.Batch batch;

        public Frame(final GlyphEffectRenderer renderer) {
            this.renderer = renderer;
        }

        public Frame setup(final RenderTarget target) {
            this.target = target;
            return this;
        }

        @Override
        public void accept(final GlyphRenderParameters parameters) {
            GlyphEffectRenderer.Batch batch = this.batch;
            if (batch == null) {
                this.batch = batch = renderer.startBatch(target);
            }

            batch.render(parameters);
        }

        @Override
        public void close() {
            final GlyphEffectRenderer.Batch batch = this.batch;
            if (batch != null) {
                batch.close();
            }

            target = null;
            this.batch = null;
        }
    }
}
