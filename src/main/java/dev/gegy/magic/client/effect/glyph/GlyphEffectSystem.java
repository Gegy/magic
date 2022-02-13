package dev.gegy.magic.client.effect.glyph;

import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class GlyphEffectSystem implements EffectSystem {
    private final GlyphEffectRenderer renderer;
    private final Frame frame;

    private final GlyphRenderParameters parameters = new GlyphRenderParameters();

    private GlyphEffectSystem(GlyphEffectRenderer renderer) {
        this.renderer = renderer;
        this.frame = new Frame(renderer);
    }

    public static GlyphEffectSystem create(ResourceManager resources) throws IOException {
        var renderer = GlyphEffectRenderer.create(resources);
        return new GlyphEffectSystem(renderer);
    }

    @Override
    public void render(MinecraftClient client, WorldRenderContext context, Framebuffer targetFramebuffer, EffectSelector effects) {
        // TODO: frustum culling

        try (var frame = this.frame.setup(targetFramebuffer)) {
            var parameters = this.parameters;
            for (var effect : effects.select(GlyphsEffect.TYPE)) {
                effect.render(parameters, context, frame);
            }
        }
    }

    @Override
    public void close() {
        this.renderer.close();
    }

    private static final class Frame implements GlyphsEffect.RenderFunction, AutoCloseable {
        private final GlyphEffectRenderer renderer;

        private Framebuffer target;
        private GlyphEffectRenderer.Batch batch;

        public Frame(GlyphEffectRenderer renderer) {
            this.renderer = renderer;
        }

        public Frame setup(Framebuffer target) {
            this.target = target;
            return this;
        }

        @Override
        public void accept(GlyphRenderParameters parameters) {
            var batch = this.batch;
            if (batch == null) {
                this.batch = batch = this.renderer.startBatch(this.target);
            }

            batch.render(parameters);
        }

        @Override
        public void close() {
            var batch = this.batch;
            if (batch != null) {
                batch.close();
            }

            this.target = null;
            this.batch = null;
        }
    }
}
