package dev.gegy.magic.client.glyph;

import dev.gegy.magic.glyph.GlyphPlane;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.spell.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec2f;

public final class ClientGlyph {
    public static final float FORM_TICKS = 2;

    public final Entity source;
    public final GlyphPlane plane;

    public float radius;

    public float red = 1.0F;
    public float green = 1.0F;
    public float blue = 1.0F;

    public int shape;

    public GlyphStroke stroke;

    public final long createTime;

    public ClientGlyph(Entity source, GlyphPlane plane, float radius, long createTime) {
        this.source = source;
        this.plane = plane;
        this.radius = radius;
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

    public void applySpell(Spell spell) {
        this.red = spell.red;
        this.green = spell.green;
        this.blue = spell.blue;
    }
}
