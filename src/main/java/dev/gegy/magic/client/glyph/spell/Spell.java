package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransform;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.GlyphForm;

import java.util.List;

public record Spell(
        SpellSource source,
        SpellTransform transform,
        SpellGlyphs glyphs
) {
    public static Spell prepare(final SpellSource source, final SpellTransform transform, final List<ClientDrawingGlyph> drawingGlyphs) {
        final SpellGlyphs glyphs = new SpellGlyphs();

        for (int index = 0; index < drawingGlyphs.size(); index++) {
            final ClientDrawingGlyph drawingGlyph = drawingGlyphs.get(index);

            final GlyphForm form = drawingGlyph.asForm();
            final GlyphTransform targetTransform = transform.getTransformForGlyph(index);

            glyphs.add(new SpellCastingGlyph(source, form, targetTransform));
        }

        return new Spell(source, transform, glyphs);
    }

    public void tick() {
        transform.tick();
    }
}
