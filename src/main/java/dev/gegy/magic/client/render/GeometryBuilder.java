package dev.gegy.magic.client.render;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferVertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import dev.gegy.magic.client.render.gl.GlGeometry;

import java.util.function.Consumer;

public final class GeometryBuilder {
    public static final VertexFormatElement POSITION_2F_ELEMENT = new VertexFormatElement(0, VertexFormatElement.Type.FLOAT, VertexFormatElement.Usage.POSITION, 2);

    public static final VertexFormat POSITION_2F = new VertexFormat(ImmutableMap.of("Position", POSITION_2F_ELEMENT));

    public static GlGeometry uploadQuadPos2f(float min, float max) {
        return upload(builder -> {
            builder.begin(VertexFormat.Mode.QUADS, GeometryBuilder.POSITION_2F);
            vertex2f(builder, min, min);
            vertex2f(builder, min, max);
            vertex2f(builder, max, max);
            vertex2f(builder, max, min);
        });
    }

    public static GlGeometry upload(Consumer<BufferBuilder> builderFunction) {
        var builder = new BufferBuilder(64);
        builderFunction.accept(builder);
        return GlGeometry.upload(builder.end());
    }

    public static void vertex2f(BufferVertexConsumer builder, float x, float y) {
        Preconditions.checkState(builder.currentElement() == POSITION_2F_ELEMENT, "invalid element");
        builder.putFloat(0, x);
        builder.putFloat(4, y);
        builder.nextElement();
        builder.endVertex();
    }
}
