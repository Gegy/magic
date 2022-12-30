package dev.gegy.magic.client.render.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import org.lwjgl.opengl.GL15;

public final class GlGeometry implements GlBindableObject {
    private final GlBuffer vertexBuffer;
    private final RenderSystem.IndexBuffer indexBuffer;
    private final GlVertexArray vertexArray;

    private final int drawMode;
    private final int vertexCount;

    private final Binding binding = new Binding();

    private GlGeometry(
            GlBuffer vertexBuffer, RenderSystem.IndexBuffer indexBuffer, GlVertexArray vertexArray,
            int drawMode, int vertexCount
    ) {
        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.vertexArray = vertexArray;
        this.drawMode = drawMode;
        this.vertexCount = vertexCount;
    }

    public static GlGeometry upload(BufferBuilder builder) {
        var data = builder.popData();
        var parameters = data.getFirst();
        var bytes = data.getSecond();

        int drawMode = parameters.getMode().mode;
        int vertexCount = parameters.getVertexCount();

        var vertexBuffer = GlBuffer.generate(GlBuffer.Target.VERTICES, GlBuffer.Usage.STATIC_DRAW);
        var vertexArray = GlVertexArray.generate();

        var indexBuffer = RenderSystem.getSequentialBuffer(parameters.getMode(), vertexCount);

        BufferRenderer.unbindAll();

        try (var vertexArrayBinding = vertexArray.bind()) {
            var vertexBufferBinding = vertexBuffer.bind();

            bytes.limit(parameters.getLimit());
            vertexBufferBinding.put(bytes);

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.getId());

            vertexArrayBinding.enableFormat(parameters.getVertexFormat());

            return new GlGeometry(vertexBuffer, indexBuffer, vertexArray, drawMode, vertexCount);
        }
    }

    @Override
    public Binding bind() {
        var binding = this.binding;
        binding.bind();
        return binding;
    }

    @Override
    public void delete() {
        this.vertexBuffer.delete();
        this.vertexArray.delete();
    }

    public final class Binding implements GlBinding {
        private GlVertexArray.Binding vertexArrayBinding;

        private Binding() {
        }

        private void bind() {
            BufferRenderer.unbindAll();
            this.vertexArrayBinding = GlGeometry.this.vertexArray.bind();
        }

        public void draw() {
            RenderSystem.drawElements(GlGeometry.this.drawMode, GlGeometry.this.vertexCount, GlGeometry.this.indexBuffer.getElementFormat().count);
        }

        @Override
        public void unbind() {
            var vertexArrayBinding = this.vertexArrayBinding;
            if (vertexArrayBinding == null) {
                throw new IllegalStateException("cannot unbind geometry - it is already unbound!");
            }

            vertexArrayBinding.unbind();

            this.vertexArrayBinding = null;
        }
    }
}
