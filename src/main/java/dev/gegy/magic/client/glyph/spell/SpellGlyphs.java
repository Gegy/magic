package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class SpellGlyphs extends AbstractList<SpellCastingGlyph> {
    private static final float GLYPH_SPACING = 0.2F;

    private final List<SpellCastingGlyph> glyphs = new ArrayList<>();

    public static float getDistanceForGlyph(final int index) {
        return GlyphTransform.DRAW_DISTANCE + index * GLYPH_SPACING;
    }

    @Override
    public boolean add(final SpellCastingGlyph glyph) {
        return glyphs.add(glyph);
    }

    @Override
    public boolean remove(final Object obj) {
        return glyphs.remove(obj);
    }

    @Override
    public SpellCastingGlyph remove(final int index) {
        return glyphs.remove(index);
    }

    @Override
    public SpellCastingGlyph get(final int index) {
        return glyphs.get(index);
    }

    @Override
    public int size() {
        return glyphs.size();
    }

    @Override
    public Iterator<SpellCastingGlyph> iterator() {
        return glyphs.iterator();
    }
}
