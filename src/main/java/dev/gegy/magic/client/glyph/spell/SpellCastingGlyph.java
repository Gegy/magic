package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.GlyphForm;

public final record SpellCastingGlyph(
        SpellSource source,
        GlyphForm form,
        GlyphTransform transform
) {
}
