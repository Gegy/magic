package dev.gegy.magic.casting.drawing;

import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public final record DrawingGlyphParameters(
        Vec3f direction,
        float radius,
        int shape,
        @Nullable GlyphType formedGlyphType
) {
    public static final PacketCodec<DrawingGlyphParameters> CODEC = PacketCodec.of(DrawingGlyphParameters::encode, DrawingGlyphParameters::decode);

    public boolean isFormed() {
        return this.formedGlyphType != null;
    }

    private void encode(PacketByteBuf buf) {
        PacketCodec.VEC3F.encode(this.direction, buf);
        buf.writeFloat(this.radius);
        buf.writeShort(this.shape);
        GlyphType.PACKET_CODEC.encode(this.formedGlyphType, buf);
    }

    private static DrawingGlyphParameters decode(PacketByteBuf buf) {
        var direction = PacketCodec.VEC3F.decode(buf);
        float radius = buf.readFloat();
        int shape = buf.readShort();
        var formedGlyphType = GlyphType.PACKET_CODEC.decode(buf);
        return new DrawingGlyphParameters(direction, radius, shape, formedGlyphType);
    }
}
