package dev.gegy.magic.client.effect.casting.spell.teleport;

import dev.gegy.magic.casting.spell.teleport.TeleportTargetSymbol;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.SpellCastingGlyph;
import dev.gegy.magic.math.ColorRgb;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collection;
import java.util.List;

public record TeleportEffect(
        SpellSource source,
        GlyphPlane sourcePlane,
        List<Symbol> symbols,
        TeleportTargetAnimator animator,
        long createTime
) implements Effect {
    public static final float SYMBOL_SIZE = 0.5F;

    public static final EffectType<TeleportEffect> TYPE = EffectType.create();

    public static TeleportEffect create(SpellCastingGlyph sourceGlyph, Collection<TeleportTargetSymbol> targets, long createTime) {
        var sourceRadius = sourceGlyph.form().radius();

        var sourcePlane = new GlyphPlane();
        sourcePlane.set(sourceGlyph.transform());

        var symbols = targets.stream().map(Symbol::create).toList();
        var animator = new TeleportTargetAnimator(sourceRadius + (SYMBOL_SIZE / 2.0F), symbols.size());

        return new TeleportEffect(sourceGlyph.source(), sourcePlane, symbols, animator, createTime);
    }

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }

    public final record Symbol(
            FormattedCharSequence text,
            ColorRgb innerColor, ColorRgb outlineColor,
            float offsetX, float offsetY
    ) {
        private static final Minecraft CLIENT = Minecraft.getInstance();

        static Symbol create(TeleportTargetSymbol symbol) {
            var textRenderer = CLIENT.font;

            var text = FormattedCharSequence.codepoint(symbol.character(), Style.EMPTY);

            var innerColor = getInnerColor(symbol.color());
            var outerColor = getOuterColor(symbol.color());

            float offsetX = -textRenderer.width(text) / 2.0F;
            float offsetY = -textRenderer.lineHeight / 2.0F;

            return new Symbol(text, innerColor, outerColor, offsetX, offsetY);
        }

        private static ColorRgb getInnerColor(ColorRgb color) {
            var hsluv = color.toHsluv();
            hsluv = hsluv.warmHue(30.0F / 360.0F)
                    .withSaturation(0.6F)
                    .withLight(0.85F);
            return hsluv.toRgb();
        }

        private static ColorRgb getOuterColor(ColorRgb color) {
            var hsluv = color.toHsluv();
            hsluv = hsluv
                    .withSaturation(0.9F)
                    .withLight(0.55F);
            return hsluv.toRgb();
        }
    }
}
