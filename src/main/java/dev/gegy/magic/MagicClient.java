package dev.gegy.magic;

import dev.gegy.magic.client.draw.GlyphDrawHandler;
import dev.gegy.magic.client.particle.MagicParticles;
import net.fabricmc.api.ClientModInitializer;

public final class MagicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        MagicParticles.onInitializeClient();
        GlyphDrawHandler.onInitialize();
    }
}
