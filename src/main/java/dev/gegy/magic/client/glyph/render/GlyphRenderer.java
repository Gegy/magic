package dev.gegy.magic.client.glyph.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.glyph.render.shader.GlyphTextureShader;
import dev.gegy.magic.client.glyph.render.shader.GlyphWorldShader;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public final class GlyphRenderer implements AutoCloseable {
    private static final VertexFormatElement POSITION_ELEMENT = new VertexFormatElement(0, VertexFormatElement.Format.FLOAT, VertexFormatElement.Type.POSITION, 2);
    private static final VertexFormat VERTEX_FORMAT = new VertexFormat(ImmutableList.of(POSITION_ELEMENT));

    private final GlyphWorldShader worldShader;
    private final GlyphTextureShader textureShader;
    private final VertexBuffer geometry;

    private final GlyphTexture texture;

    private final Batcher batcher = new Batcher();

    private GlyphRenderer(GlyphWorldShader worldShader, GlyphTextureShader textureShader, VertexBuffer geometry, GlyphTexture texture) {
        this.worldShader = worldShader;
        this.textureShader = textureShader;
        this.geometry = geometry;
        this.texture = texture;
    }

    public static GlyphRenderer create(ResourceManager resources) throws IOException {
        GlyphWorldShader worldShader = GlyphWorldShader.create(resources);
        GlyphTextureShader textureShader = GlyphTextureShader.create(resources);

        VertexBuffer worldGeometry = uploadGeometry();

        GlyphTexture texture = GlyphTexture.create();

        return new GlyphRenderer(worldShader, textureShader, worldGeometry, texture);
    }

    private static VertexBuffer uploadGeometry() {
        VertexBuffer geometry = new VertexBuffer(VERTEX_FORMAT);

        BufferBuilder builder = Tessellator.getInstance().getBuffer();
        builder.begin(GL11.GL_QUADS, VERTEX_FORMAT);
        putVertex(builder, -1.0F, -1.0F);
        putVertex(builder, -1.0F, 1.0F);
        putVertex(builder, 1.0F, 1.0F);
        putVertex(builder, 1.0F, -1.0F);
        builder.end();

        geometry.upload(builder);

        return geometry;
    }

    private static void putVertex(BufferBuilder builder, float x, float y) {
        builder.putFloat(0, x);
        builder.putFloat(4, y);
        builder.nextElement();
        builder.next();
    }

    public Batcher start(Framebuffer framebuffer, Matrix4f worldToScreen) {
        Batcher batcher = this.batcher;
        batcher.start(framebuffer, worldToScreen);
        return batcher;
    }

    @Override
    public void close() {
        this.worldShader.close();
        this.textureShader.close();

        this.texture.close();

        this.geometry.close();
    }

    public final class Batcher implements AutoCloseable {
        private Framebuffer framebuffer;
        private Matrix4f worldToScreen;

        void start(Framebuffer framebuffer, Matrix4f worldToScreen) {
            this.framebuffer = framebuffer;
            this.worldToScreen = worldToScreen;

            RenderSystem.disableCull();

            GlyphRenderer.this.geometry.bind();
            VERTEX_FORMAT.startDrawing(0);
        }

        public void render(GlyphRenderData renderData, float tickDelta) {
            this.renderToTexture(renderData, tickDelta);

            this.framebuffer.beginWrite(true);
            this.renderToWorld(renderData);
        }

        private void renderToTexture(GlyphRenderData renderData, float tickDelta) {
            GlyphTexture texture = GlyphRenderer.this.texture;
            GlyphTextureShader textureShader = GlyphRenderer.this.textureShader;

            texture.bindWrite();

            GlStateManager.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT, false);

            textureShader.bind(texture, renderData, tickDelta);
            RenderSystem.drawArrays(GL11.GL_QUADS, 0, 4);
            textureShader.unbind();

            texture.unbindWrite();
        }

        private void renderToWorld(GlyphRenderData renderData) {
            GlyphTexture texture = GlyphRenderer.this.texture;
            GlyphWorldShader shader = GlyphRenderer.this.worldShader;

            texture.bindRead();

            shader.bind(texture, this.worldToScreen, renderData);
            RenderSystem.drawArrays(GL11.GL_QUADS, 0, 4);
            shader.unbind();

            texture.unbindRead();
        }

        @Override
        public void close() {
            VertexBuffer.unbind();
            VERTEX_FORMAT.endDrawing();

            RenderSystem.enableCull();

            this.framebuffer = null;
            this.worldToScreen = null;
        }
    }
}
