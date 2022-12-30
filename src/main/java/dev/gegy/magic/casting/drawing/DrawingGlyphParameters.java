package dev.gegy.magic.casting.drawing;

import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public record DrawingGlyphParameters(
        Vector3f direction,
        float radius,
        int shape,
        @Nullable GlyphType formedGlyphType
) {
    public static final PacketCodec<DrawingGlyphParameters> CODEC = PacketCodec.of(DrawingGlyphParameters::encode, DrawingGlyphParameters::decode);

    public boolean isFormed() {
        return formedGlyphType != null;
    }

    private void encode(final FriendlyByteBuf buf) {
        PacketCodec.VEC3F.encode(direction, buf);
        buf.writeFloat(radius);
        buf.writeShort(shape);
        GlyphType.PACKET_CODEC.encode(formedGlyphType, buf);
    }

    private static DrawingGlyphParameters decode(final FriendlyByteBuf buf) {
        final Vector3f direction = PacketCodec.VEC3F.decode(buf);
        final float radius = buf.readFloat();
        final int shape = buf.readShort();
        final GlyphType formedGlyphType = GlyphType.PACKET_CODEC.decode(buf);
        return new DrawingGlyphParameters(direction, radius, shape, formedGlyphType);
    }
}
