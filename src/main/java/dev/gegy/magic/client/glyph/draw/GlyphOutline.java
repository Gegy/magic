package dev.gegy.magic.client.glyph.draw;

import net.minecraft.util.math.Matrix3f;

final class GlyphOutline {
    final Matrix3f worldToGlyph = new Matrix3f();
    final Matrix3f glyphToWorld = new Matrix3f();

    float centerX;
    float centerY;
    float radius;
}
