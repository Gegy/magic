package dev.gegy.magic.client.glyph;

import java.nio.FloatBuffer;

public record GlyphStroke(float x0, float y0, float x1, float y1) {
    public void writeToBuffer(final FloatBuffer buffer) {
        buffer.put(x0).put(y0);
        buffer.put(x1).put(y1);
    }
}
