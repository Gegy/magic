package dev.gegy.magic.client.effect.glyph;

import dev.gegy.magic.client.casting.drawing.FadingGlyph;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.spell.Spell;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

import java.util.Collection;

public interface GlyphsEffect extends Effect {
    EffectType<GlyphsEffect> TYPE = EffectType.create();

    static GlyphsEffect fromSpell(Spell spell) {
        return homogenous(spell.glyphs(), GlyphRenderParameters::setSpell);
    }

    static GlyphsEffect fromFading(Collection<FadingGlyph> fadingGlyphs) {
        return homogenous(fadingGlyphs, GlyphRenderParameters::setFading);
    }

    static <T> GlyphsEffect homogenous(Iterable<T> glyphs, GlyphRenderParameters.Applicator<T> parametersApplicator) {
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

    final record Homogenous<T>(
            Iterable<T> glyphs,
            GlyphRenderParameters.Applicator<T> parametersApplicator
    ) implements GlyphsEffect {
        @Override
        public void render(GlyphRenderParameters parameters, WorldRenderContext context, RenderFunction render) {
            var parametersApplicator = this.parametersApplicator;
            for (var glyph : this.glyphs) {
                parametersApplicator.set(parameters, glyph, context);
                render.accept(parameters);
            }
        }
    }
}
