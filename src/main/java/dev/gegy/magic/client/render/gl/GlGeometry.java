package dev.gegy.magic.client.render.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;

public final class GlGeometry implements GlBindableObject {
    private final GlBuffer vertexBuffer;
    private final RenderSystem.AutoStorageIndexBuffer indexBuffer;
    private final GlVertexArray vertexArray;

    private final int drawMode;
    private final int vertexCount;

    private final Binding binding = new Binding();

    private GlGeometry(
            final GlBuffer vertexBuffer, final RenderSystem.AutoStorageIndexBuffer indexBuffer, final GlVertexArray vertexArray,
            final int drawMode, final int vertexCount
    ) {
        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.vertexArray = vertexArray;
        this.drawMode = drawMode;
        this.vertexCount = vertexCount;
    }

    public static GlGeometry upload(final BufferBuilder.RenderedBuffer buffer) {
        final BufferBuilder.DrawState parameters = buffer.drawState();

        final int drawMode = parameters.mode().asGLMode;
        final int vertexCount = parameters.indexCount();

        final GlVertexArray vertexArray = GlVertexArray.generate();
        final GlBuffer vertexBuffer = GlBuffer.generate(GlBuffer.Target.VERTICES, GlBuffer.Usage.STATIC_DRAW);
        final RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(parameters.mode());

        try (final GlVertexArray.Binding vertexArrayBinding = vertexArray.bind()) {
            final GlBuffer.Binding vertexBufferBinding = vertexBuffer.bind();
            vertexBufferBinding.put(buffer.vertexBuffer());
            indexBuffer.bind(vertexCount);

            vertexArrayBinding.enableFormat(parameters.format());

            return new GlGeometry(vertexBuffer, indexBuffer, vertexArray, drawMode, vertexCount);
        }
    }

    @Override
    public Binding bind() {
        final Binding binding = this.binding;
        binding.bind();
        return binding;
    }

    @Override
    public void delete() {
        vertexBuffer.delete();
        vertexArray.delete();
    }

    public final class Binding implements GlBinding {
        private GlVertexArray.Binding vertexArrayBinding;

        private Binding() {
        }

        private void bind() {
            vertexArrayBinding = vertexArray.bind();
        }

        public void draw() {
            RenderSystem.drawElements(drawMode, vertexCount, indexBuffer.type().asGLType);
        }

        @Override
        public void unbind() {
            final GlVertexArray.Binding vertexArrayBinding = this.vertexArrayBinding;
            if (vertexArrayBinding == null) {
                throw new IllegalStateException("cannot unbind geometry - it is already unbound!");
            }

            vertexArrayBinding.unbind();
            this.vertexArrayBinding = null;
        }
    }
}
