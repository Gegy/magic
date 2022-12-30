package dev.gegy.magic;

import dev.gegy.magic.client.casting.ClientCastingTracker;
import dev.gegy.magic.client.casting.ClientCastingType;
import dev.gegy.magic.client.effect.EffectManager;
import dev.gegy.magic.client.particle.MagicParticles;
import dev.gegy.magic.network.s2c.MagicS2CNetworking;
import net.fabricmc.api.ClientModInitializer;

public final class MagicClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EffectManager.onInitialize();
        ClientCastingType.onInitialize();

        MagicS2CNetworking.registerReceivers();

        MagicParticles.onInitializeClient();

        ClientCastingTracker.register();
    }
}
