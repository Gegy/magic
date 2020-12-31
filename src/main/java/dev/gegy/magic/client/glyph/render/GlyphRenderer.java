package dev.gegy.magic.client.glyph.render;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
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

    private final GlyphShader shader;
    private final VertexBuffer geometry;

    private final Batcher batcher = new Batcher();

    private GlyphRenderer(GlyphShader shader, VertexBuffer geometry) {
        this.shader = shader;
        this.geometry = geometry;
    }

    public static GlyphRenderer create(ResourceManager resources) throws IOException {
        GlyphShader shader = GlyphShader.create(resources);
        VertexBuffer geometry = uploadGeometry();

        return new GlyphRenderer(shader, geometry);
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

    public Batcher start(Matrix4f worldToScreen) {
        Batcher batcher = this.batcher;
        batcher.start(worldToScreen);
        return batcher;
    }

    @Override
    public void close() {
        this.shader.close();
        this.geometry.close();
    }

    public final class Batcher implements AutoCloseable {
        void start(Matrix4f worldToScreen) {
            RenderSystem.disableCull();
            RenderSystem.enableBlend();

            GlyphRenderer.this.shader.bind(worldToScreen);

            GlyphRenderer.this.geometry.bind();
            VERTEX_FORMAT.startDrawing(0);
        }

        public void render(Matrix4f glyphToWorld, float centerX, float centerY, float radius, float formProgress, float red, float green, float blue, int edges) {
            GlyphRenderer.this.shader.set(glyphToWorld, centerX, centerY, radius, formProgress, red, green, blue, edges);
            RenderSystem.drawArrays(GL11.GL_QUADS, 0, 4);
        }

        @Override
        public void close() {
            VertexBuffer.unbind();
            VERTEX_FORMAT.endDrawing();

            GlyphRenderer.this.shader.unbind();

            RenderSystem.enableCull();
            RenderSystem.disableBlend();
        }
    }
}
