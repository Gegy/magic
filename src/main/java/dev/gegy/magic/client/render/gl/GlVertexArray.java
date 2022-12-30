package dev.gegy.magic.client.render.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.opengl.GL30;

public final class GlVertexArray implements GlBindableObject {
    private final int id;

    private final Binding binding = new Binding();

    private VertexFormat format;

    private GlVertexArray(final int id) {
        this.id = id;
    }

    public static GlVertexArray generate() {
        final int id = GL30.glGenVertexArrays();
        return new GlVertexArray(id);
    }

    @Override
    public Binding bind() {
        BufferUploader.invalidate();
        GL30.glBindVertexArray(id);
        return binding;
    }

    @Override
    public void delete() {
        GlStateManager._glDeleteBuffers(id);
    }

    private void enableFormat(final VertexFormat format) {
        disableFormat();

        format.setupBufferState();
        this.format = format;
    }

    private void disableFormat() {
        final VertexFormat format = this.format;
        if (format != null) {
            this.format = null;
            format.clearBufferState();
        }
    }

    public final class Binding implements GlBinding {
        private Binding() {
        }

        public void enableFormat(final VertexFormat format) {
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
