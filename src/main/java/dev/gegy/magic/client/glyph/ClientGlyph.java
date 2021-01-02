package dev.gegy.magic.client.glyph;

import dev.gegy.magic.glyph.GlyphPlane;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec2f;

public final class ClientGlyph {
    public static final float FORM_TICKS = 2;

    public final Entity source;
    public final GlyphPlane plane;

    public float radius;

    public float red;
    public float green;
    public float blue;

    public int shape;

    public GlyphStroke stroke;

    public final long createTime;

    ClientGlyph(
            Entity source, GlyphPlane plane,
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

    public boolean tick() {
        GlyphStroke stroke = this.stroke;
        if (stroke != null) {
            stroke.tick();
        }
        return this.source.removed;
    }

    public boolean putEdge(GlyphEdge edge) {
        int bit = edge.asBit();
        if ((this.shape & bit) == 0) {
            this.shape |= bit;
            return true;
        } else {
            return false;
        }
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
