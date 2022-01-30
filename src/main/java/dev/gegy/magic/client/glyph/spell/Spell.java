package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.SpellSource;

import java.util.ArrayList;
import java.util.List;

public record Spell(
        SpellSource source,
        SpellTransform transform,
        List<PreparedGlyph> glyphs
) {
    public static Spell prepare(SpellSource source, List<ClientDrawingGlyph> drawingGlyphs) {
        float castingDistance = SpellTransform.getDistanceForGlyph(drawingGlyphs.size());
        var spellTransform = new SpellTransform(source.getLookVector(1.0F), castingDistance);

        var glyphs = new ArrayList<PreparedGlyph>(drawingGlyphs.size());

        for (int index = 0; index < drawingGlyphs.size(); index++) {
            var drawingGlyph = drawingGlyphs.get(index);

            var form = drawingGlyph.asForm();
            var targetTransform = spellTransform.getTransformForGlyph(index);

            glyphs.add(new PreparedGlyph(source, form, targetTransform));
        }

        return new Spell(source, spellTransform, glyphs);
    }

    public void tick() {
        var look = this.source.getLookVector(1.0F);
        this.transform.tick(look);
    }
}
