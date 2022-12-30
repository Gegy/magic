package dev.gegy.magic.client.render.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.BufferRenderer;
import org.lwjgl.opengl.GL15;

import java.nio.ByteBuffer;

public final class GlBuffer implements GlBindableObject {
    private final int id;

    private final int target;
    private final int usage;

    private final Binding binding = new Binding();

    private GlBuffer(int id, int target, int usage) {
        this.id = id;
        this.target = target;
        this.usage = usage;
    }

    public static GlBuffer generate(Target target, Usage usage) {
        int id = GL15.glGenBuffers();
        return new GlBuffer(id, target.id, usage.id);
    }

    @Override
    public Binding bind() {
        BufferRenderer.resetCurrentVertexBuffer();
        GL15.glBindBuffer(this.target, this.id);
        return this.binding;
    }

    @Override
    public void delete() {
        GlStateManager._glDeleteBuffers(this.id);
    }

    public final class Binding implements GlBinding {
        private Binding() {
        }

        public void put(ByteBuffer data) {
            GL15.glBufferData(GlBuffer.this.target, data, GlBuffer.this.usage);
        }

        @Override
        public void unbind() {
            GL15.glBindBuffer(GlBuffer.this.target, 0);
        }
    }

    public enum Target {
        VERTICES(GL15.GL_ARRAY_BUFFER),
        INDICES(GL15.GL_ELEMENT_ARRAY_BUFFER);

        private final int id;

        Target(int id) {
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

        Usage(int id) {
            this.id = id;
        }
    }
}
