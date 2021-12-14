package dev.gegy.magic.client.spellcasting.draw;

import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import dev.gegy.magic.network.c2s.CancelSpellC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;

final class PreparedSpell implements SpellDrawState {
    @Override
    public SpellDrawState tick(ClientPlayerEntity player) {
        if (player.isSneaking()) {
            ClientSpellcastingTracker.INSTANCE.clearGlyphsFor(player);
            CancelSpellC2SPacket.sendToServer();
            return new BeginDraw();
        }

        return this;
    }
}
