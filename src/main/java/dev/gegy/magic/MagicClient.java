package dev.gegy.magic;

import dev.gegy.magic.client.render.glyph.GlyphRenderManager;
import dev.gegy.magic.network.s2c.MagicS2CNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class MagicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        GlyphRenderManager.onInitialize();

        MagicS2CNetworking.registerReceivers();
    }
}
