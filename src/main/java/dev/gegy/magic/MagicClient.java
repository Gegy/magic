package dev.gegy.magic;

import dev.gegy.magic.client.glyph.render.GlyphRenderManager;
import dev.gegy.magic.client.particle.MagicParticles;
import dev.gegy.magic.network.s2c.MagicS2CNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class MagicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MagicParticles.onInitializeClient();
        GlyphRenderManager.onInitialize();

        MagicS2CNetworking.registerReceivers();
    }
}
