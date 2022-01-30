package dev.gegy.magic.client.glyph;

import java.nio.FloatBuffer;

public final record GlyphStroke(float x0, float y0, float x1, float y1) {
    public void writeToBuffer(FloatBuffer buffer) {
        buffer.put(this.x0).put(this.y0);
        buffer.put(this.x1).put(this.y1);
    }
}
