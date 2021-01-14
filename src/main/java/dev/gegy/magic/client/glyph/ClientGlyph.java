package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.glyph.plane.GlyphTransform;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.spell.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec2f;

public final class ClientGlyph {
    public static final float FORM_TICKS = 2;

    public final Entity source;
    public GlyphTransform transform;

    public float radius;

    private final GlyphColor primaryColor = new GlyphColor(GlyphColor.DEFAULT_PRIMARY);
    private final GlyphColor secondaryColor = new GlyphColor(GlyphColor.DEFAULT_SECONDARY);

    public int shape;

    public GlyphStroke stroke;

    public final long createTime;

    public ClientGlyph(Entity source, GlyphTransform transform, float radius, long createTime) {
        this.source = source;
        this.transform = transform;
        this.radius = radius;
        this.createTime = createTime;
    }

    public boolean tick() {
        this.primaryColor.tick(0.15F);
        this.secondaryColor.tick(0.15F);

        GlyphStroke stroke = this.stroke;
        if (stroke != null) {
            stroke.tick();
        }

        this.transform.tick();

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
        this.primaryColor.set(spell.red, spell.green, spell.blue);
        this.secondaryColor.set(GlyphColor.primaryToSecondary(spell.red, spell.green, spell.blue));
    }

    public GlyphColor getPrimaryColor() {
        return this.primaryColor;
    }

    public GlyphColor getSecondaryColor() {
        return this.secondaryColor;
    }
}
