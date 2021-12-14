package dev.gegy.magic.client.effect.glyph;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.shader.EffectTexture;
import dev.gegy.magic.client.render.GeometryBuilder;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class GlyphEffects implements AutoCloseable {
    private final GlyphWorldShader worldShader;
    private final VertexBuffer geometry;

    private final EffectTexture<GlyphRenderParameters> texture;

    private final Renderer renderer = new Renderer();

    private GlyphEffects(GlyphWorldShader worldShader, VertexBuffer geometry, EffectTexture<GlyphRenderParameters> texture) {
        this.worldShader = worldShader;
        this.geometry = geometry;
        this.texture = texture;
    }

    public static GlyphEffects create(ResourceManager resources) throws IOException {
        GlyphWorldShader worldShader = GlyphWorldShader.create(resources);
        GlyphTextureShader textureShader = GlyphTextureShader.create(resources);
        VertexBuffer geometry = GeometryBuilder.uploadQuadPos2f(-1.0F, 1.0F);
        EffectTexture<GlyphRenderParameters> texture = EffectTexture.create(textureShader, GlyphTexture.SIZE);

        return new GlyphEffects(worldShader, geometry, texture);
    }

    public Renderer renderTo(Framebuffer target) {
        Renderer renderer = this.renderer;
        renderer.start(target);
        return renderer;
    }

    @Override
    public void close() {
        this.worldShader.close();
        this.geometry.close();
        this.texture.close();
    }

    public final class Renderer implements AutoCloseable {
        private Framebuffer target;

        void start(Framebuffer target) {
            this.target = target;

            RenderSystem.disableCull();

            GlyphEffects.this.geometry.bind();
            GeometryBuilder.POSITION_2F.startDrawing();
        }

        public void render(GlyphRenderParameters parameters) {
            GlyphEffects.this.texture.renderWith(parameters, GlyphEffects.this.geometry);

            this.target.beginWrite(true);
            this.renderToWorld(parameters);
        }

        private void renderToWorld(GlyphRenderParameters parameters) {
            EffectTexture<GlyphRenderParameters> texture = GlyphEffects.this.texture;
            GlyphWorldShader shader = GlyphEffects.this.worldShader;

            texture.bindRead();
            shader.bind(parameters);

            GlyphEffects.this.geometry.drawElements();

            shader.unbind();
            texture.unbindRead();
        }

        @Override
        public void close() {
            GeometryBuilder.POSITION_2F.endDrawing();
            VertexBuffer.unbind();

            RenderSystem.enableCull();

            this.target = null;
        }
    }
}
