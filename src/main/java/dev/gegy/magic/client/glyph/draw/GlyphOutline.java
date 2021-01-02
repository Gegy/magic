package dev.gegy.magic.client.glyph.draw;

import dev.gegy.magic.glyph.GlyphPlane;

final class GlyphOutline {
    final GlyphPlane plane;
    final float radius;

    GlyphOutline(GlyphPlane plane, float radius) {
        this.plane = plane;
        this.radius = radius;
    }
}
