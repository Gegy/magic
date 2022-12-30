package dev.gegy.magic.glyph;

import dev.gegy.magic.math.ColorHsluv;
import dev.gegy.magic.math.ColorRgb;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public final record GlyphStyle(ColorRgb primaryColor, ColorRgb secondaryColor) {
    public static final PacketCodec<GlyphStyle> PACKET_CODEC = PacketCodec.of(GlyphStyle::encode, GlyphStyle::decode);

    public static final GlyphStyle WILD = new GlyphStyle(ColorRgb.of(0x364684), ColorRgb.of(0xD3DFE5));

    public static final GlyphStyle RED = GlyphStyle.of(10.0F / 360.0F);
    public static final GlyphStyle PURPLE = GlyphStyle.of(280.0F / 360.0F);

    public static GlyphStyle of(float hue) {
        var primary = ColorHsluv.of(hue, 0.85F, 0.45F);
        var secondary = ColorHsluv.of(ColorHsluv.warmHue(hue, 30.0F / 360.0F), 0.6F, 0.85F);
        return new GlyphStyle(primary.toRgb(), secondary.toRgb());
    }

    private void encode(FriendlyByteBuf buf) {
        ColorRgb.PACKET_CODEC.encode(this.primaryColor, buf);
        ColorRgb.PACKET_CODEC.encode(this.secondaryColor, buf);
    }

    private static GlyphStyle decode(FriendlyByteBuf buf) {
        var primaryColor = ColorRgb.PACKET_CODEC.decode(buf);
        var secondaryColor = ColorRgb.PACKET_CODEC.decode(buf);
        return new GlyphStyle(primaryColor, secondaryColor);
    }
}
