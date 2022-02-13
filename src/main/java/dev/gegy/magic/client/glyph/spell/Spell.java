package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransform;

import java.util.List;

public final record Spell(
        SpellSource source,
        SpellTransform transform,
        SpellGlyphs glyphs
) {
    public static Spell prepare(SpellSource source, SpellTransform transform, List<ClientDrawingGlyph> drawingGlyphs) {
        var glyphs = new SpellGlyphs();

        for (int index = 0; index < drawingGlyphs.size(); index++) {
            var drawingGlyph = drawingGlyphs.get(index);

            var form = drawingGlyph.asForm();
            var targetTransform = transform.getTransformForGlyph(index);

            glyphs.add(new SpellCastingGlyph(source, form, targetTransform));
        }

        return new Spell(source, transform, glyphs);
    }

    public void tick() {
        this.transform.tick();
    }
}
