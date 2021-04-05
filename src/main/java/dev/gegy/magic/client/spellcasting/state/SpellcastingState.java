package dev.gegy.magic.client.spellcasting.state;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.spell.Spell;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface SpellcastingState {
    SpellcastingState tick(ClientPlayerEntity player);

    default SpellcastingState finishDrawingGlyph(Spell spell, Consumer<ClientGlyph> yield) {
        return this;
    }

    @Nullable
    default ClientGlyph getDrawingGlyph() {
        return null;
    }
}
