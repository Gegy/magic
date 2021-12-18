package dev.gegy.magic.client.spellcasting.state;

import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import dev.gegy.magic.network.c2s.CancelSpellC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;

final class PreparedSpell implements SpellCastState {
    @Override
    public SpellCastState tick(ClientPlayerEntity player) {
        if (player.isSneaking()) {
            ClientSpellcastingTracker.INSTANCE.clearGlyphsFor(player);
            CancelSpellC2SPacket.sendToServer();
            return new BeginDraw();
        }

        return this;
    }
}
