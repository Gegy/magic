package dev.gegy.magic.client.glyph;

import dev.gegy.magic.glyph.GlyphPlane;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.spell.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public final class ClientGlyph {
    public static final float FORM_TICKS = 2;

    private static final float DEFAULT_RED = 1.0F;
    private static final float DEFAULT_GREEN = 1.0F;
    private static final float DEFAULT_BLUE = 0.7F;

    public final Entity source;
    public final GlyphPlane plane;

    public float radius;

    private float red = DEFAULT_RED, green = DEFAULT_GREEN, blue = DEFAULT_BLUE;
    private float targetRed = this.red, targetGreen = this.green, targetBlue = this.blue;
    private float prevRed = this.red, prevGreen = this.green, prevBlue = this.blue;

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
        this.prevRed = this.red;
        this.prevGreen = this.green;
        this.prevBlue = this.blue;

        this.red += (this.targetRed - this.red) * 0.15F;
        this.green += (this.targetGreen - this.green) * 0.15F;
        this.blue += (this.targetBlue - this.blue) * 0.15F;

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
        this.targetRed = spell.red;
        this.targetGreen = spell.green;
        this.targetBlue = spell.blue;
    }

    public float getRed(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevRed, this.red);
    }

    public float getGreen(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevGreen, this.green);
    }

    public float getBlue(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevBlue, this.blue);
    }
}
