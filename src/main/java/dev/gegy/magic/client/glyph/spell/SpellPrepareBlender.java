package dev.gegy.magic.client.glyph.spell;

import com.google.common.base.Preconditions;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.transform.BlendingGlyphTransform;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.math.AnimationTimer;

import java.util.List;

public final class SpellPrepareBlender {
    public static final int LENGTH = 6;

    private final Spell spell;
    private final AnimationTimer timer;

    private SpellPrepareBlender(final Spell spell, final AnimationTimer timer) {
        this.spell = spell;
        this.timer = timer;
    }

    public static SpellPrepareBlender create(final List<ClientDrawingGlyph> drawingGlyphs, final Spell spell) {
        Preconditions.checkState(spell.glyphs().size() == drawingGlyphs.size(), "mismatched drawing and prepared glyphs");

        final SpellGlyphs blendingGlyphs = new SpellGlyphs();
        final Spell blendingSpell = new Spell(spell.source(), spell.transform(), blendingGlyphs);

        final AnimationTimer timer = new AnimationTimer(LENGTH);

        for (int index = 0; index < drawingGlyphs.size(); index++) {
            final SpellCastingGlyph glyph = spell.glyphs().get(index);
            final ClientDrawingGlyph drawingGlyph = drawingGlyphs.get(index);

            final GlyphTransform sourceTransform = drawingGlyph.plane().asTransform();
            final GlyphTransform targetTransform = spell.transform().getTransformForGlyph(index);

            final BlendingGlyphTransform blendingTransform = new BlendingGlyphTransform(sourceTransform, targetTransform, timer);
            blendingGlyphs.add(new SpellCastingGlyph(glyph.source(), glyph.form(), blendingTransform));
        }

        return new SpellPrepareBlender(blendingSpell, timer);
    }

    public boolean tick() {
        spell.tick();
        return timer.tick();
    }

    public Spell spell() {
        return spell;
    }
}
