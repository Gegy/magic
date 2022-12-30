package dev.gegy.magic.client.render.gl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;

public final class GlGeometry implements GlBindableObject {
    private final GlBuffer vertexBuffer;
    private final RenderSystem.ShapeIndexBuffer indexBuffer;
    private final GlVertexArray vertexArray;

    private final int drawMode;
    private final int vertexCount;

    private final Binding binding = new Binding();

    private GlGeometry(
            GlBuffer vertexBuffer, RenderSystem.ShapeIndexBuffer indexBuffer, GlVertexArray vertexArray,
            int drawMode, int vertexCount
    ) {
        this.vertexBuffer = vertexBuffer;
        this.indexBuffer = indexBuffer;
        this.vertexArray = vertexArray;
        this.drawMode = drawMode;
        this.vertexCount = vertexCount;
    }

    public static GlGeometry upload(BufferBuilder.BuiltBuffer buffer) {
        var parameters = buffer.getParameters();

        int drawMode = parameters.mode().glMode;
        int vertexCount = parameters.indexCount();

        var vertexArray = GlVertexArray.generate();
        var vertexBuffer = GlBuffer.generate(GlBuffer.Target.VERTICES, GlBuffer.Usage.STATIC_DRAW);
        var indexBuffer = RenderSystem.getSequentialBuffer(parameters.mode());

        try (var vertexArrayBinding = vertexArray.bind()) {
            var vertexBufferBinding = vertexBuffer.bind();
            vertexBufferBinding.put(buffer.getVertexBuffer());
            indexBuffer.bindAndGrow(vertexCount);

            vertexArrayBinding.enableFormat(parameters.format());

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
            this.vertexArrayBinding = GlGeometry.this.vertexArray.bind();
        }

        public void draw() {
            RenderSystem.drawElements(GlGeometry.this.drawMode, GlGeometry.this.vertexCount, GlGeometry.this.indexBuffer.getIndexType().glType);
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
