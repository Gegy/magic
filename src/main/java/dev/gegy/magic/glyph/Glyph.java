package dev.gegy.magic.glyph;

import dev.gegy.magic.glyph.shape.GlyphEdge;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public final class Glyph {
    public static final float FORM_TICKS = 2;

    // TODO: can and should center and radius be apart of the matrix?
    public final Vec3d source;
    public final Matrix4f glyphToWorld;
    public final Matrix3f worldToGlyph;

    public final float centerX;
    public final float centerY;
    public float radius;

    public float red;
    public float green;
    public float blue;

    public int edges;

    public final long createTime;

    public Glyph(
            Vec3d source, Matrix4f glyphToWorld, Matrix3f worldToGlyph,
            float centerX, float centerY, float radius,
            float red, float green, float blue,
            long createTime
    ) {
        this.source = source;
        this.glyphToWorld = glyphToWorld;
        this.worldToGlyph = worldToGlyph;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.createTime = createTime;
    }

    public void putEdge(GlyphEdge edge) {
        this.edges |= edge.asBit();
    }

    public float getFormProgress(long time, float tickDelta) {
        float age = (float) (time - this.createTime) + tickDelta;
        return Math.min(age / FORM_TICKS, 1.0F);
    }
}
