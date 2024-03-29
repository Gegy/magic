package dev.gegy.magic.client.render;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferVertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

import java.util.function.Consumer;

public final class MagicGeometry {
    public static final VertexFormatElement POSITION_2F_ELEMENT = new VertexFormatElement(0, VertexFormatElement.Format.FLOAT, VertexFormatElement.Type.POSITION, 2);

    public static final VertexFormat POSITION_2F = new VertexFormat(ImmutableMap.of("Position", POSITION_2F_ELEMENT));

    public static VertexBuffer uploadQuadPos2f(float min, float max) {
        return upload(builder -> {
            builder.begin(VertexFormat.DrawMode.QUADS, MagicGeometry.POSITION_2F);
            vertex2f(builder, min, min);
            vertex2f(builder, min, max);
            vertex2f(builder, max, max);
            vertex2f(builder, max, min);
        });
    }

    public static VertexBuffer upload(Consumer<BufferBuilder> builderFunction) {
        BufferBuilder builder = new BufferBuilder(64);
        builderFunction.accept(builder);
        builder.end();

        VertexBuffer geometry = new VertexBuffer();
        geometry.upload(builder);

        return geometry;
    }

    public static void vertex2f(BufferVertexConsumer builder, float x, float y) {
        Preconditions.checkState(builder.getCurrentElement() == POSITION_2F_ELEMENT, "invalid element");
        builder.putFloat(0, x);
        builder.putFloat(4, y);
        builder.nextElement();
        builder.next();
    }
}
