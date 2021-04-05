package dev.gegy.magic.client.spellcasting.state;

import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.network.c2s.CancelSpellC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;

public final class PreparedSpell implements SpellcastingState {
    @Override
    public SpellcastingState tick(ClientPlayerEntity player) {
        if (player.isSneaking()) {
            ClientGlyphTracker.INSTANCE.clearGlyphsFor(player);
            CancelSpellC2SPacket.sendToServer();
            return new BeginSpellcasting();
        }

        return this;
    }
}
