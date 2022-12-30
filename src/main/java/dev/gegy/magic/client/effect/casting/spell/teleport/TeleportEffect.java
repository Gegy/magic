package dev.gegy.magic.client.effect.casting.spell.teleport;

import dev.gegy.magic.casting.spell.teleport.TeleportTargetSymbol;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.SpellCastingGlyph;
import dev.gegy.magic.math.ColorHsluv;
import dev.gegy.magic.math.ColorRgb;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    public static final float SYMBOL_SIZE = 0.5f;

    public static final EffectType<TeleportEffect> TYPE = EffectType.create();

    public static TeleportEffect create(final SpellCastingGlyph sourceGlyph, final Collection<TeleportTargetSymbol> targets, final long createTime) {
        final float sourceRadius = sourceGlyph.form().radius();

        final GlyphPlane sourcePlane = new GlyphPlane();
        sourcePlane.set(sourceGlyph.transform());

        final List<Symbol> symbols = targets.stream().map(Symbol::create).toList();
        final TeleportTargetAnimator animator = new TeleportTargetAnimator(sourceRadius + (SYMBOL_SIZE / 2.0f), symbols.size());

        return new TeleportEffect(sourceGlyph.source(), sourcePlane, symbols, animator, createTime);
    }

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }

    public record Symbol(
            FormattedCharSequence text,
            ColorRgb innerColor, ColorRgb outlineColor,
            float offsetX, float offsetY
    ) {
        private static final Minecraft CLIENT = Minecraft.getInstance();

        static Symbol create(final TeleportTargetSymbol symbol) {
            final Font textRenderer = CLIENT.font;

            final FormattedCharSequence text = FormattedCharSequence.codepoint(symbol.character(), Style.EMPTY);

            final ColorRgb innerColor = getInnerColor(symbol.color());
            final ColorRgb outerColor = getOuterColor(symbol.color());

            final float offsetX = -textRenderer.width(text) / 2.0f;
            final float offsetY = -textRenderer.lineHeight / 2.0f;

            return new Symbol(text, innerColor, outerColor, offsetX, offsetY);
        }

        private static ColorRgb getInnerColor(final ColorRgb color) {
            ColorHsluv hsluv = color.toHsluv();
            hsluv = hsluv.warmHue(30.0f / 360.0f)
                    .withSaturation(0.6f)
                    .withLight(0.85f);
            return hsluv.toRgb();
        }

        private static ColorRgb getOuterColor(final ColorRgb color) {
            ColorHsluv hsluv = color.toHsluv();
            hsluv = hsluv
                    .withSaturation(0.9f)
                    .withLight(0.55f);
            return hsluv.toRgb();
        }
    }
}
