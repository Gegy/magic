package dev.gegy.magic.client.draw;

import net.minecraft.util.math.Matrix3f;

final class GlyphOutline {
    final Matrix3f projectWorldToGlyph = new Matrix3f();
    final Matrix3f projectGlyphToWorld = new Matrix3f();

    float centerX;
    float centerY;
    float radius;
}
