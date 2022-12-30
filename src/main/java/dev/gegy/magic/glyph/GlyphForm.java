package dev.gegy.magic.glyph;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public final record GlyphForm(
        float radius,
        int shape,
        GlyphStyle style
) {
    public static final PacketCodec<GlyphForm> PACKET_CODEC = PacketCodec.of(GlyphForm::encode, GlyphForm::decode);

    private void encode(FriendlyByteBuf buf) {
        buf.writeFloat(this.radius);
        buf.writeShort(this.shape);
        GlyphStyle.PACKET_CODEC.encode(this.style, buf);
    }

    private static GlyphForm decode(FriendlyByteBuf buf) {
        float radius = buf.readFloat();
        int shape = buf.readShort();
        var style = GlyphStyle.PACKET_CODEC.decode(buf);
        return new GlyphForm(radius, shape, style);
    }
}
