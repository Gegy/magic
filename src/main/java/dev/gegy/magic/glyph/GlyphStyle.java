package dev.gegy.magic.glyph;

import dev.gegy.magic.math.ColorHsluv;
import dev.gegy.magic.math.ColorRgb;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public record GlyphStyle(ColorRgb primaryColor, ColorRgb secondaryColor) {
    public static final PacketCodec<GlyphStyle> PACKET_CODEC = PacketCodec.of(GlyphStyle::encode, GlyphStyle::decode);

    public static final GlyphStyle WILD = new GlyphStyle(ColorRgb.of(0x364684), ColorRgb.of(0xD3DFE5));

    public static final GlyphStyle RED = GlyphStyle.of(10.0f / 360.0f);
    public static final GlyphStyle PURPLE = GlyphStyle.of(280.0f / 360.0f);

    public static GlyphStyle of(final float hue) {
        final ColorHsluv primary = ColorHsluv.of(hue, 0.85f, 0.45f);
        final ColorHsluv secondary = ColorHsluv.of(ColorHsluv.warmHue(hue, 30.0f / 360.0f), 0.6f, 0.85f);
        return new GlyphStyle(primary.toRgb(), secondary.toRgb());
    }

    private void encode(final FriendlyByteBuf buf) {
        ColorRgb.PACKET_CODEC.encode(primaryColor, buf);
        ColorRgb.PACKET_CODEC.encode(secondaryColor, buf);
    }

    private static GlyphStyle decode(final FriendlyByteBuf buf) {
        final ColorRgb primaryColor = ColorRgb.PACKET_CODEC.decode(buf);
        final ColorRgb secondaryColor = ColorRgb.PACKET_CODEC.decode(buf);
        return new GlyphStyle(primaryColor, secondaryColor);
    }
}
