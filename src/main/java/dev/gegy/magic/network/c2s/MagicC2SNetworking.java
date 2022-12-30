package dev.gegy.magic.network.c2s;

import dev.gegy.magic.casting.ServerCastingTracker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public final class MagicC2SNetworking {
    public static void registerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(CastingEventC2SPacket.CHANNEL, (server, player, handler, buf, responseSender) -> {
            final ResourceLocation id = buf.readResourceLocation();
            buf.retain();

            server.submit(() -> {
                try {
                    ServerCastingTracker.INSTANCE.handleEvent(player, id, buf);
                } finally {
                    buf.release();
                }
            });
        });
    }
}
