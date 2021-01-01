package dev.gegy.magic.glyph;

import dev.gegy.magic.client.glyph.draw.GlyphPlane;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public final class Glyph {
    public static final float FORM_TICKS = 2;

    public final Vec3d source;
    public final GlyphPlane plane;

    public float radius;

    public float red;
    public float green;
    public float blue;

    public int edges;

    public GlyphStroke stroke;

    public final long createTime;

    public Glyph(
            Vec3d source, GlyphPlane plane,
            float radius,
            float red, float green, float blue,
            long createTime
    ) {
        this.source = source;
        this.plane = plane;
        this.radius = radius;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.createTime = createTime;
    }

    public void tick() {
        if (this.stroke != null) {
            this.stroke.tick();
        }
    }

    public void putEdge(GlyphEdge edge) {
        this.edges |= edge.asBit();
    }

    public float getFormProgress(long time, float tickDelta) {
        float age = (float) (time - this.createTime) + tickDelta;
        return Math.min(age / FORM_TICKS, 1.0F);
    }

    public GlyphStroke startStroke(Vec2f from) {
        GlyphStroke stroke = new GlyphStroke(from.x, from.y);
        this.stroke = stroke;
        return stroke;
    }

    public void stopStroke() {
        this.stroke = null;
    }
}
