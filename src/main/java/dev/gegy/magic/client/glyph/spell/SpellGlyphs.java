package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SpellGlyphs extends AbstractList<SpellCastingGlyph> {
    private static final float GLYPH_SPACING = 0.2F;

    private final List<SpellCastingGlyph> glyphs = new ArrayList<>();

    public static float getDistanceForGlyph(int index) {
        return GlyphTransform.DRAW_DISTANCE + index * GLYPH_SPACING;
    }

    @Override
    public boolean add(SpellCastingGlyph glyph) {
        return this.glyphs.add(glyph);
    }

    @Override
    public boolean remove(Object obj) {
        return this.glyphs.remove(obj);
    }

    @Override
    public SpellCastingGlyph remove(int index) {
        return this.glyphs.remove(index);
    }

    @Override
    public SpellCastingGlyph get(int index) {
        return this.glyphs.get(index);
    }

    @Override
    public int size() {
        return this.glyphs.size();
    }

    @Override
    public Iterator<SpellCastingGlyph> iterator() {
        return this.glyphs.iterator();
    }
}
