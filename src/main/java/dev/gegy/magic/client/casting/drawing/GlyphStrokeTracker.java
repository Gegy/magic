package dev.gegy.magic.client.casting.drawing;

import dev.gegy.magic.client.glyph.GlyphStroke;
import net.minecraft.util.Mth;

final class GlyphStrokeTracker {
    private final float fromX;
    private final float fromY;
    private float toX;
    private float toY;
    private float prevToX;
    private float prevToY;

    public GlyphStrokeTracker(final float fromX, final float fromY) {
        this.fromX = toX = prevToX = fromX;
        this.fromY = toY = prevToY = fromY;
    }

    public void tick(final float x, final float y) {
        prevToX = toX;
        prevToY = toY;
        toX = x;
        toY = y;
    }

    public GlyphStroke resolve(final float tickDelta) {
        final float toX = Mth.lerp(tickDelta, prevToX, this.toX);
        final float toY = Mth.lerp(tickDelta, prevToY, this.toY);
        return new GlyphStroke(fromX, fromY, toX, toY);
    }
}
