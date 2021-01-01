package dev.gegy.magic.client.glyph.draw;

import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Matrix4f;

public final class GlyphPlane {
    private final Matrix4f glyphToWorld;
    private final Matrix4f worldToGlyph;

    GlyphPlane(Matrix4f glyphToWorld, Matrix4f worldToGlyph) {
        this.glyphToWorld = glyphToWorld;
        this.worldToGlyph = worldToGlyph;
    }

    public Matrix4f getGlyphToWorld() {
        return this.glyphToWorld;
    }

    public void transformWorldToGlyph(Vector4f vector) {
        vector.transform(this.worldToGlyph);

        float x = vector.getX();
        float y = vector.getY();
        float z = vector.getZ();
        vector.set(x / z, y / z, 1.0F, 1.0F);
    }
}
