package dev.gegy.magic.glyph;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public record GlyphForm(
        float radius,
        int shape,
        GlyphStyle style
) {
    public static final PacketCodec<GlyphForm> PACKET_CODEC = PacketCodec.of(GlyphForm::encode, GlyphForm::decode);

    private void encode(final FriendlyByteBuf buf) {
        buf.writeFloat(radius);
        buf.writeShort(shape);
        GlyphStyle.PACKET_CODEC.encode(style, buf);
    }

    private static GlyphForm decode(final FriendlyByteBuf buf) {
        final float radius = buf.readFloat();
        final int shape = buf.readShort();
        final GlyphStyle style = GlyphStyle.PACKET_CODEC.decode(buf);
        return new GlyphForm(radius, shape, style);
    }
}
