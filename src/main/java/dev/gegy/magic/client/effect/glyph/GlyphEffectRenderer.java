package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.shader.EffectTexture;
import dev.gegy.magic.client.render.GeometryBuilder;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class GlyphEffectRenderer implements AutoCloseable {
    private final GlyphWorldShader worldShader;
    private final VertexBuffer geometry;

    private final EffectTexture<GlyphRenderParameters> texture;

    private final Batch batch = new Batch();

    private GlyphEffectRenderer(GlyphWorldShader worldShader, VertexBuffer geometry, EffectTexture<GlyphRenderParameters> texture) {
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
        this.worldShader.close();
        this.geometry.close();
        this.texture.close();
    }

    public final class Batch implements AutoCloseable {
        private Framebuffer target;

        void start(Framebuffer target) {
            this.target = target;

            RenderSystem.disableCull();
            RenderSystem.enableBlend();

            GlyphEffectRenderer.this.geometry.bind();
            GeometryBuilder.POSITION_2F.startDrawing();
        }

        public void render(GlyphRenderParameters parameters) {
            GlyphEffectRenderer.this.texture.renderWith(parameters, GlyphEffectRenderer.this.geometry);

            this.target.beginWrite(true);
            this.renderToWorld(parameters);
        }

        private void renderToWorld(GlyphRenderParameters parameters) {
            var texture = GlyphEffectRenderer.this.texture;
            var shader = GlyphEffectRenderer.this.worldShader;

            texture.bindRead();
            shader.bind(parameters);

            GlyphEffectRenderer.this.geometry.drawElements();

            shader.unbind();
            texture.unbindRead();
        }

        @Override
        public void close() {
            GeometryBuilder.POSITION_2F.endDrawing();
            VertexBuffer.unbind();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();

            this.target = null;
        }
    }
}
