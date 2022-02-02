package dev.gegy.magic.client.effect.glyph;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.casting.drawing.FadingGlyph;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.SpellCastingGlyph;

import java.util.Collection;

public final record GlyphEffect<T>(
        Collection<T> glyphs,
        GlyphRenderParameters.Applicator<T> parametersApplicator
) implements Effect {
    public static final EffectType<GlyphEffect<?>> TYPE = EffectType.create();

    public static GlyphEffect<ClientDrawingGlyph> drawing(Collection<ClientDrawingGlyph> glyphs) {
        return new GlyphEffect<>(glyphs, GlyphRenderParameters::setDrawing);
    }

    public static GlyphEffect<SpellCastingGlyph> spell(Spell spell) {
        return new GlyphEffect<>(spell.glyphs(), GlyphRenderParameters::setSpell);
    }

    public static GlyphEffect<FadingGlyph> fading(Collection<FadingGlyph> glyphs) {
        return new GlyphEffect<>(glyphs, GlyphRenderParameters::setFading);
    }

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }
}
