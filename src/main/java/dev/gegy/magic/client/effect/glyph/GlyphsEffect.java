package dev.gegy.magic.client.effect.glyph;

import dev.gegy.magic.client.casting.drawing.FadingGlyph;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.spell.Spell;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.Collection;

public interface GlyphsEffect extends Effect {
    EffectType<GlyphsEffect> TYPE = EffectType.create();

    static GlyphsEffect fromSpell(final Spell spell) {
        return homogenous(spell.glyphs(), GlyphRenderParameters::setSpell);
    }

    static GlyphsEffect fromFading(final Collection<FadingGlyph> fadingGlyphs) {
        return homogenous(fadingGlyphs, GlyphRenderParameters::setFading);
    }

    static <T> GlyphsEffect homogenous(final Iterable<T> glyphs, final GlyphRenderParameters.Applicator<T> parametersApplicator) {
        return new Homogenous<>(glyphs, parametersApplicator);
    }

    void render(GlyphRenderParameters parameters, WorldRenderContext context, RenderFunction render);

    @Override
    default EffectType<?> getType() {
        return TYPE;
    }

    interface RenderFunction {
        void accept(GlyphRenderParameters parameters);
    }

    record Homogenous<T>(
            Iterable<T> glyphs,
            GlyphRenderParameters.Applicator<T> parametersApplicator
    ) implements GlyphsEffect {
        @Override
        public void render(final GlyphRenderParameters parameters, final WorldRenderContext context, final RenderFunction render) {
            final GlyphRenderParameters.Applicator<T> parametersApplicator = this.parametersApplicator;
            for (final T glyph : glyphs) {
                parametersApplicator.set(parameters, glyph, context);
                render.accept(parameters);
            }
        }
    }
}
