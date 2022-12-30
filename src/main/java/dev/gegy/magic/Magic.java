package dev.gegy.magic;

import dev.gegy.magic.casting.ServerCastingTracker;
import dev.gegy.magic.client.particle.MagicParticles;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.network.c2s.MagicC2SNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Magic implements ModInitializer {
    public static final String ID = "magic";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        MagicC2SNetworking.registerReceivers();

        MagicParticles.onInitialize();

        ServerCastingTracker.register();
        GlyphType.onInitialize();
    }

    public static ResourceLocation identifier(String id) {
        return new ResourceLocation(ID, id);
    }
}
