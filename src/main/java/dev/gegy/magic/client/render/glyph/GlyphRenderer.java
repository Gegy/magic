package dev.gegy.magic.client.render.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.render.MagicGeometry;
import dev.gegy.magic.client.render.shader.EffectTexture;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class GlyphRenderer implements AutoCloseable {
    private final GlyphWorldEffect worldEffect;
    private final VertexBuffer geometry;

    private final EffectTexture<GlyphRenderData> texture;

    private final Batcher batcher = new Batcher();

    private GlyphRenderer(GlyphWorldEffect worldEffect, VertexBuffer geometry, EffectTexture<GlyphRenderData> texture) {
        this.worldEffect = worldEffect;
        this.geometry = geometry;
        this.texture = texture;
    }

    public static GlyphRenderer create(ResourceManager resources) throws IOException {
        GlyphWorldEffect worldEffect = GlyphWorldEffect.create(resources);
        GlyphTextureEffect textureEffect = GlyphTextureEffect.create(resources);
        VertexBuffer geometry = MagicGeometry.uploadQuadPos2f(-1.0F, 1.0F);
        EffectTexture<GlyphRenderData> texture = EffectTexture.create(textureEffect, GlyphTexture.SIZE);

        return new GlyphRenderer(worldEffect, geometry, texture);
    }

    public Batcher start(Framebuffer target) {
        Batcher batcher = this.batcher;
        batcher.start(target);
        return batcher;
    }

    @Override
    public void close() {
        this.worldEffect.close();
        this.geometry.close();
        this.texture.close();
    }

    public final class Batcher implements AutoCloseable {
        private Framebuffer target;

        void start(Framebuffer target) {
            this.target = target;

            RenderSystem.disableCull();

            GlyphRenderer.this.geometry.bind();
            MagicGeometry.POSITION_2F.startDrawing();
        }

        public void render(GlyphRenderData renderData) {
            GlyphRenderer.this.texture.renderWith(renderData, GlyphRenderer.this.geometry);

            this.target.beginWrite(true);
            this.renderToWorld(renderData);
        }

        private void renderToWorld(GlyphRenderData renderData) {
            EffectTexture<GlyphRenderData> texture = GlyphRenderer.this.texture;
            GlyphWorldEffect shader = GlyphRenderer.this.worldEffect;

            texture.bindRead();
            shader.bind(renderData);

            GlyphRenderer.this.geometry.method_35665();

            shader.unbind();
            texture.unbindRead();
        }

        @Override
        public void close() {
            MagicGeometry.POSITION_2F.endDrawing();
            VertexBuffer.unbind();

            RenderSystem.enableCull();

            this.target = null;
        }
    }
}
