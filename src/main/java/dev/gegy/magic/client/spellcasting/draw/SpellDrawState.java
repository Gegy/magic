package dev.gegy.magic.client.spellcasting.draw;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.spellcasting.Spell;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

interface SpellDrawState {
    SpellDrawState tick(ClientPlayerEntity player);

    default FinishDrawingResult finishDrawingGlyph(Spell spell) {
        return FinishDrawingResult.noGlyph(this);
    }

    @Nullable
    default ClientGlyph getDrawingGlyph() {
        return null;
    }

    final record FinishDrawingResult(
            SpellDrawState state,
            @Nullable ClientGlyph glyph
    ) {
        static FinishDrawingResult noGlyph(SpellDrawState state) {
            return new FinishDrawingResult(state, null);
        }

        static FinishDrawingResult finishGlyph(SpellDrawState state, ClientGlyph glyph) {
            return new FinishDrawingResult(state, glyph);
        }
    }
}
