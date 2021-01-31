package dev.gegy.magic;

import dev.gegy.magic.network.c2s.MagicC2SNetworking;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class Magic implements ModInitializer {
    public static final String ID = "magic";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    @Override
    public void onInitialize() {
        MagicC2SNetworking.registerReceivers();
    }

    public static Identifier identifier(String id) {
        return new Identifier(ID, id);
    }
}
