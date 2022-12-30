package dev.gegy.magic.client.render.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.VertexFormat;
import org.lwjgl.opengl.GL30;

public final class GlVertexArray implements GlBindableObject {
    private final int id;

    private final Binding binding = new Binding();

    private VertexFormat format;

    private GlVertexArray(int id) {
        this.id = id;
    }

    public static GlVertexArray generate() {
        int id = GL30.glGenVertexArrays();
        return new GlVertexArray(id);
    }

    @Override
    public Binding bind() {
        BufferRenderer.resetCurrentVertexBuffer();
        GL30.glBindVertexArray(this.id);
        return this.binding;
    }

    @Override
    public void delete() {
        GlStateManager._glDeleteBuffers(this.id);
    }

    private void enableFormat(VertexFormat format) {
        this.disableFormat();

        format.setupState();
        this.format = format;
    }

    private void disableFormat() {
        var format = this.format;
        if (format != null) {
            this.format = null;
            format.clearState();
        }
    }

    public final class Binding implements GlBinding {
        private Binding() {
        }

        public void enableFormat(VertexFormat format) {
            GlVertexArray.this.enableFormat(format);
        }

        public void disableFormat() {
            GlVertexArray.this.disableFormat();
        }

        @Override
        public void unbind() {
            GL30.glBindVertexArray(0);
        }
    }
}
