package dev.gegy.magic;

import dev.gegy.magic.casting.ServerCastingTracker;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.network.c2s.MagicC2SNetworking;
import dev.gegy.magic.particle.MagicParticles;
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

        MagicParticles.register();

        ServerCastingTracker.register();
        GlyphType.onInitialize();
    }

    public static ResourceLocation identifier(final String id) {
        return new ResourceLocation(ID, id);
    }
}
