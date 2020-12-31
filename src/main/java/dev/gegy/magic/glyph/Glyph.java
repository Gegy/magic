package dev.gegy.magic.glyph;

import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public final class Glyph {
    public static final float FORM_TICKS = 2;

    public final Vec3d source;
    public final Matrix4f glyphToWorld;

    // TODO: can and should center and radius be apart of the matrix?
    public final float centerX;
    public final float centerY;
    public final float radius;
    public final float red;
    public final float green;
    public final float blue;

    public final long createTime;

    public Glyph(Vec3d source, Matrix4f glyphToWorld, float centerX, float centerY, float radius, float red, float green, float blue, long createTime) {
        this.source = source;
        this.glyphToWorld = glyphToWorld;
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.createTime = createTime;
    }

    public float getFormProgress(long time, float tickDelta) {
        float age = (float) (time - this.createTime) + tickDelta;
        return Math.min(age / FORM_TICKS, 1.0F);
    }
}
