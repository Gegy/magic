package dev.gegy.magic.glyph;

import dev.gegy.magic.math.ColorRgb;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public final record GlyphStyle(ColorRgb primaryColor, ColorRgb secondaryColor) {
    public static final GlyphStyle UNDIFFERENTIATED = GlyphStyle.of(0xFFE633);

    public static final GlyphStyle CYAN = GlyphStyle.of(0x19E6FF);
    public static final GlyphStyle RED = GlyphStyle.of(0xFF4C19);
    public static final GlyphStyle PURPLE = GlyphStyle.of(0xD419FF);
    public static final GlyphStyle GREEN = GlyphStyle.of(0x3FFF19);

    public static GlyphStyle of(int packed) {
        var primary = new ColorRgb(packed);
        var secondary = primaryToSecondary(primary);
        return new GlyphStyle(primary, secondary);
    }

    private static ColorRgb primaryToSecondary(ColorRgb primary) {
        var buffer = new float[3];

        Color.RGBtoHSB(
                MathHelper.floor(primary.red() * 255.0F),
                MathHelper.floor(primary.green() * 255.0F),
                MathHelper.floor(primary.blue() * 255.0F),
                buffer
        );
        float hue = buffer[0];
        float saturation = buffer[1];
        float brightness = buffer[2];

        saturation *= 0.3F;
        hue = warmHue(hue, 0.05F);

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        return new ColorRgb(rgb);
    }

    private static float warmHue(float hue, float amount) {
        if (hue < amount || hue > 0.65F) {
            hue += amount;
        } else {
            hue -= amount;
        }

        if (hue > 1.0F) hue -= 1.0F;
        if (hue < 0.0F) hue += 1.0F;

        return hue;
    }
}
