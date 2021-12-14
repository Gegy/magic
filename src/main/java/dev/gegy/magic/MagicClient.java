package dev.gegy.magic;

import dev.gegy.magic.client.particle.MagicParticles;
import dev.gegy.magic.client.render.EffectRenderManager;
import dev.gegy.magic.network.s2c.MagicS2CNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class MagicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EffectRenderManager.onInitialize();

        MagicS2CNetworking.registerReceivers();

        MagicParticles.onInitializeClient();
    }
}
