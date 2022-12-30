package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.shader.EffectTexture;
import dev.gegy.magic.client.render.GeometryBuilder;
import dev.gegy.magic.client.render.gl.GlBinding;
import dev.gegy.magic.client.render.gl.GlGeometry;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public final class GlyphEffectRenderer implements AutoCloseable {
    private final GlyphWorldShader worldShader;
    private final GlGeometry geometry;

    private final EffectTexture<GlyphRenderParameters> texture;

    private final Batch batch = new Batch();

    private GlyphEffectRenderer(final GlyphWorldShader worldShader, final GlGeometry geometry, final EffectTexture<GlyphRenderParameters> texture) {
        this.worldShader = worldShader;
        this.geometry = geometry;
        this.texture = texture;
    }

    public static GlyphEffectRenderer create(final ResourceManager resources) throws IOException {
        final GlyphWorldShader worldShader = GlyphWorldShader.create(resources);
        final GlyphTextureShader textureShader = GlyphTextureShader.create(resources);
        final GlGeometry geometry = GeometryBuilder.uploadQuadPos2f(-1.0f, 1.0f);
        final EffectTexture<GlyphRenderParameters> texture = EffectTexture.create(textureShader, GlyphTexture.SIZE);

        return new GlyphEffectRenderer(worldShader, geometry, texture);
    }

    public Batch startBatch(final RenderTarget target) {
        final Batch batch = this.batch;
        batch.start(target);
        return batch;
    }

    @Override
    public void close() {
        worldShader.delete();
        geometry.delete();
        texture.delete();
    }

    public final class Batch implements AutoCloseable {
        private RenderTarget target;
        private GlGeometry.Binding geometryBinding;

        void start(final RenderTarget target) {
            this.target = target;

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            geometryBinding = geometry.bind();
        }

        public void render(final GlyphRenderParameters parameters) {
            texture.renderWith(parameters, geometryBinding);

            target.bindWrite(true);
            renderToWorld(parameters);
        }

        private void renderToWorld(final GlyphRenderParameters parameters) {
            try (
                    final EffectTexture.ReadBinding textureBinding = texture.bindRead();
                    final GlBinding shaderBinding = worldShader.bind(parameters)
            ) {
                geometryBinding.draw();
            }
        }

        @Override
        public void close() {
            geometryBinding.unbind();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();

            geometryBinding = null;
            target = null;
        }
    }
}
