package dev.gegy.magic.client.spellcasting;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.spellcasting.state.BeginSpellcasting;
import dev.gegy.magic.client.spellcasting.state.SpellcastingState;
import dev.gegy.magic.spell.Spell;
import net.minecraft.client.network.ClientPlayerEntity;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

public final class SpellcastingController {
    private SpellcastingState state;

    public void tick(ClientPlayerEntity player) {
        SpellcastingState state = this.state;
        if (state == null) {
            this.state = state = new BeginSpellcasting();
        }
        this.state = state.tick(player);
    }

    public void clear() {
        this.state = null;
    }

    @Nullable
    public ClientGlyph finishDrawingGlyph(Spell spell) {
        SpellcastingState state = this.state;
        if (state == null) {
            return null;
        }

        MutableObject<ClientGlyph> glyph = new MutableObject<>();
        this.state = state.finishDrawingGlyph(spell, glyph::setValue);

        return glyph.getValue();
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        SpellcastingState state = this.state;
        if (state != null) {
            return state.getDrawingGlyph();
        }
        return null;
    }
}
