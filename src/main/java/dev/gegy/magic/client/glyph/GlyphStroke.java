package dev.gegy.magic.client.glyph;

import net.minecraft.util.math.MathHelper;

import java.nio.FloatBuffer;

public final class GlyphStroke {
    private final float fromX;
    private final float fromY;
    private float toX;
    private float toY;
    private float prevToX;
    private float prevToY;

    public GlyphStroke(float fromX, float fromY) {
        this.fromX = this.toX = this.prevToX = fromX;
        this.fromY = this.toY = this.prevToY = fromY;
    }

    public void tick(float x, float y) {
        this.prevToX = this.toX;
        this.prevToY = this.toY;
        this.toX = x;
        this.toY = y;
    }

    public void writeToBuffer(FloatBuffer buffer, float tickDelta) {
        float toX = MathHelper.lerp(tickDelta, this.prevToX, this.toX);
        float toY = MathHelper.lerp(tickDelta, this.prevToY, this.toY);

        buffer.put(this.fromX).put(this.fromY);
        buffer.put(toX).put(toY);
    }
}
