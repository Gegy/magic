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

        try (
                var vertexBufferBinding = vertexBuffer.bind();
                var vertexArrayBinding = vertexArray.bind()
        ) {
            vertexArrayBinding.enableFormat(parameters.getVertexFormat());

            bytes.limit(parameters.getLimit());
            vertexBufferBinding.put(bytes);

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
        private GlBuffer.Binding vertexBufferBinding;
        private int indexFormat;

        private Binding() {
        }

        private void bind() {
            BufferRenderer.unbindAll();
            this.vertexArrayBinding = GlGeometry.this.vertexArray.bind();
            this.vertexBufferBinding = GlGeometry.this.vertexBuffer.bind();

            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, GlGeometry.this.indexBuffer.getId());
            this.indexFormat = GlGeometry.this.indexBuffer.getElementFormat().count;
        }

        public void draw() {
            RenderSystem.drawElements(GlGeometry.this.drawMode, GlGeometry.this.vertexCount, this.indexFormat);
        }

        @Override
        public void unbind() {
            var vertexArrayBinding = this.vertexArrayBinding;
            var vertexBufferBinding = this.vertexBufferBinding;
            if (vertexArrayBinding == null || vertexBufferBinding == null) {
                throw new IllegalStateException("cannot unbind geometry - it is already unbound!");
            }

            vertexArrayBinding.unbind();
            vertexBufferBinding.unbind();
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

            this.vertexArrayBinding = null;
            this.vertexBufferBinding = null;
        }
    }
}
