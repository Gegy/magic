package dev.gegy.magic.client.glyph.spell;

import com.google.common.base.Preconditions;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.transform.BlendingGlyphTransform;
import dev.gegy.magic.math.AnimationTimer;

import java.util.ArrayList;
import java.util.List;

public final class SpellPrepareBlender {
    public static final int LENGTH = 6;

    private final Spell spell;
    private final AnimationTimer timer;

    private SpellPrepareBlender(Spell spell, AnimationTimer timer) {
        this.spell = spell;
        this.timer = timer;
    }

    public static SpellPrepareBlender create(List<ClientDrawingGlyph> drawingGlyphs, Spell spell) {
        Preconditions.checkState(spell.glyphs().size() == drawingGlyphs.size(), "mismatched drawing and prepared glyphs");

        var blendingGlyphs = new ArrayList<PreparedGlyph>(drawingGlyphs.size());
        var blendingSpell = new Spell(spell.source(), spell.transform(), blendingGlyphs);

        var timer = new AnimationTimer(LENGTH);

        for (int index = 0; index < drawingGlyphs.size(); index++) {
            var glyph = spell.glyphs().get(index);
            var drawingGlyph = drawingGlyphs.get(index);

            var sourceTransform = drawingGlyph.plane();
            var targetTransform = spell.transform().getTransformForGlyph(index);

            var blendingTransform = new BlendingGlyphTransform(sourceTransform, targetTransform, timer);
            blendingGlyphs.add(new PreparedGlyph(glyph.source(), glyph.form(), blendingTransform));
        }

        return new SpellPrepareBlender(blendingSpell, timer);
    }

    public boolean tick() {
        this.spell.tick();
        return this.timer.tick();
    }

    public Spell spell() {
        return this.spell;
    }
}
