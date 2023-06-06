package dev.gegy.magic.glyph;

import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public record GlyphForm(
        float radius,
        GlyphShape shape,
        GlyphStyle style
) {
    public static final PacketCodec<GlyphForm> PACKET_CODEC = PacketCodec.of(GlyphForm::encode, GlyphForm::decode);

    private void encode(final FriendlyByteBuf buf) {
        buf.writeFloat(radius);
        GlyphShape.PACKET_CODEC.encode(shape, buf);
        GlyphStyle.PACKET_CODEC.encode(style, buf);
    }

    private static GlyphForm decode(final FriendlyByteBuf buf) {
        final float radius = buf.readFloat();
        final GlyphShape shape = GlyphShape.PACKET_CODEC.decode(buf);
        final GlyphStyle style = GlyphStyle.PACKET_CODEC.decode(buf);
        return new GlyphForm(radius, shape, style);
    }
}
