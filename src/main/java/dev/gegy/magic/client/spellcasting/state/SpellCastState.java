package dev.gegy.magic.client.spellcasting.state;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.glyph.GlyphType;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

interface SpellCastState {
    SpellCastState tick(ClientPlayerEntity player);

    default FinishDrawingResult finishDrawingGlyph(GlyphType matchedType) {
        return FinishDrawingResult.noGlyph(this);
    }

    @Nullable
    default ClientGlyph getDrawingGlyph() {
        return null;
    }

    final record FinishDrawingResult(
            SpellCastState state,
            @Nullable ClientGlyph glyph
    ) {
        static FinishDrawingResult noGlyph(SpellCastState state) {
            return new FinishDrawingResult(state, null);
        }

        static FinishDrawingResult finishGlyph(SpellCastState state, ClientGlyph glyph) {
            return new FinishDrawingResult(state, glyph);
        }
    }
}
