package dev.gegy.magic.client.render.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferUploader;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public final class GlBuffer implements GlBindableObject {
    private final int id;

    private final int target;
    private final int usage;

    private final Binding binding = new Binding();

    private GlBuffer(final int id, final int target, final int usage) {
        this.id = id;
        this.target = target;
        this.usage = usage;
    }

    public static GlBuffer generate(final Target target, final Usage usage) {
        final int id = GL15.glGenBuffers();
        return new GlBuffer(id, target.id, usage.id);
    }

    @Override
    public Binding bind() {
        BufferUploader.invalidate();
        GL15.glBindBuffer(target, id);
        return binding;
    }

    @Override
    public void delete() {
        GlStateManager._glDeleteBuffers(id);
    }

    public final class Binding implements GlBinding {
        private Binding() {
        }

        public void put(final ByteBuffer data) {
            GL15.glBufferData(target, data, usage);
        }

        @Override
        public void unbind() {
            GL15.glBindBuffer(target, 0);
        }
    }

    public enum Target {
        VERTICES(GL15.GL_ARRAY_BUFFER),
        INDICES(GL15.GL_ELEMENT_ARRAY_BUFFER);

        private final int id;

        Target(final int id) {
            this.id = id;
        }
    }

    public enum Usage {
        STREAM_DRAW(GL15.GL_STREAM_DRAW),
        STREAM_READ(GL15.GL_STREAM_READ),
        STREAM_COPY(GL15.GL_STREAM_COPY),
        STATIC_DRAW(GL15.GL_STATIC_DRAW),
        STATIC_READ(GL15.GL_STATIC_READ),
        STATIC_COPY(GL15.GL_STATIC_COPY),
        DYNAMIC_DRAW(GL15.GL_DYNAMIC_DRAW),
        DYNAMIC_READ(GL15.GL_DYNAMIC_READ),
        DYNAMIC_COPY(GL15.GL_DYNAMIC_COPY);

        private final int id;

        Usage(final int id) {
            this.id = id;
        }
    }
}
