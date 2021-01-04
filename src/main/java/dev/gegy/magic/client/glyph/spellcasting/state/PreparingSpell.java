package dev.gegy.magic.client.glyph.spellcasting.state;

import net.minecraft.client.network.ClientPlayerEntity;

public final class PreparingSpell implements SpellcastingState {
    @Override
    public SpellcastingState tick(ClientPlayerEntity player) {
        if (player.handSwinging) {
            return new BeginSpellcasting();
        }

        return this;
    }
}
