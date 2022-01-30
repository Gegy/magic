package dev.gegy.magic.client.casting.drawing;

import dev.gegy.magic.client.glyph.GlyphStroke;
import net.minecraft.util.math.MathHelper;

final class GlyphStrokeTracker {
    private final float fromX;
    private final float fromY;
    private float toX;
    private float toY;
    private float prevToX;
    private float prevToY;

    public GlyphStrokeTracker(float fromX, float fromY) {
        this.fromX = this.toX = this.prevToX = fromX;
        this.fromY = this.toY = this.prevToY = fromY;
    }

    public void tick(float x, float y) {
        this.prevToX = this.toX;
        this.prevToY = this.toY;
        this.toX = x;
        this.toY = y;
    }

    public GlyphStroke resolve(float tickDelta) {
        float toX = MathHelper.lerp(tickDelta, this.prevToX, this.toX);
        float toY = MathHelper.lerp(tickDelta, this.prevToY, this.toY);
        return new GlyphStroke(this.fromX, this.fromY, toX, toY);
    }
}
