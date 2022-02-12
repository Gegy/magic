package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.shader.EffectTexture;
import dev.gegy.magic.client.render.GeometryBuilder;
import dev.gegy.magic.client.render.gl.GlGeometry;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class GlyphEffectRenderer implements AutoCloseable {
    private final GlyphWorldShader worldShader;
    private final GlGeometry geometry;

    private final EffectTexture<GlyphRenderParameters> texture;

    private final Batch batch = new Batch();

    private GlyphEffectRenderer(GlyphWorldShader worldShader, GlGeometry geometry, EffectTexture<GlyphRenderParameters> texture) {
        this.worldShader = worldShader;
        this.geometry = geometry;
        this.texture = texture;
    }

    public static GlyphEffectRenderer create(ResourceManager resources) throws IOException {
        var worldShader = GlyphWorldShader.create(resources);
        var textureShader = GlyphTextureShader.create(resources);
        var geometry = GeometryBuilder.uploadQuadPos2f(-1.0F, 1.0F);
        var texture = EffectTexture.create(textureShader, GlyphTexture.SIZE);

        return new GlyphEffectRenderer(worldShader, geometry, texture);
    }

    public Batch startBatch(Framebuffer target) {
        var batch = this.batch;
        batch.start(target);
        return batch;
    }

    @Override
    public void close() {
        this.worldShader.delete();
        this.geometry.delete();
        this.texture.delete();
    }

    public final class Batch implements AutoCloseable {
        private Framebuffer target;
        private GlGeometry.Binding geometryBinding;

        void start(Framebuffer target) {
            this.target = target;

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);

            this.geometryBinding = GlyphEffectRenderer.this.geometry.bind();
        }

        public void render(GlyphRenderParameters parameters) {
            GlyphEffectRenderer.this.texture.renderWith(parameters, this.geometryBinding);

            this.target.beginWrite(true);
            this.renderToWorld(parameters);
        }

        private void renderToWorld(GlyphRenderParameters parameters) {
            try (
                    var textureBinding = GlyphEffectRenderer.this.texture.bindRead();
                    var shaderBinding = GlyphEffectRenderer.this.worldShader.bind(parameters);
            ) {
                this.geometryBinding.draw();
            }
        }

        @Override
        public void close() {
            this.geometryBinding.unbind();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();

            this.geometryBinding = null;
            this.target = null;
        }
    }
}
