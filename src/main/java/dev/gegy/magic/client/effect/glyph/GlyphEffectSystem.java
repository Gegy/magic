package dev.gegy.magic.client.effect.glyph;

import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class GlyphEffectSystem implements EffectSystem {
    private final GlyphEffectRenderer renderer;
    private final GlyphRenderParameters parameters = new GlyphRenderParameters();

    private GlyphEffectSystem(GlyphEffectRenderer renderer) {
        this.renderer = renderer;
    }

    public static GlyphEffectSystem create(ResourceManager resources) throws IOException {
        var renderer = GlyphEffectRenderer.create(resources);
        return new GlyphEffectSystem(renderer);
    }

    @Override
    public void render(MinecraftClient client, WorldRenderContext context, EffectSelector effects) {
        var glyphs = effects.select(GlyphEffect.TYPE);
        if (!this.hasAnyGlyphs(glyphs)) {
            return;
        }

        try (var batch = this.renderer.startBatch(client.getFramebuffer())) {
            for (var effect : glyphs) {
                this.renderGlyphs(context, batch, effect);
            }
        }
    }

    private boolean hasAnyGlyphs(EffectSelector.Selection<GlyphEffect<?>> glyphs) {
        for (var glyph : glyphs) {
            if (!glyph.glyphs().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private <T> void renderGlyphs(WorldRenderContext context, GlyphEffectRenderer.Batch batch, GlyphEffect<T> effect) {
        var parameters = this.parameters;

        var applicator = effect.parametersApplicator();
        for (var glyph : effect.glyphs()) {
            applicator.set(parameters, glyph, context);
            batch.render(parameters);
        }
    }

    @Override
    public void close() {
        this.renderer.close();
    }
}
