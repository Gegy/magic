package dev.gegy.magic.network.s2c;

import dev.gegy.magic.client.casting.ClientCastingTracker;
import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class MagicS2CNetworking {
    public static void registerReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SetCastingS2CPacket.CHANNEL, (client, handler, buf, responseSender) -> {
            final int sourceId = buf.readVarInt();
            final ConfiguredClientCasting<?> casting = ConfiguredClientCasting.CODEC.nullable().decode(buf);
            client.submit(() -> {
                try {
                    final Entity source = client.level.getEntity(sourceId);
                    if (source instanceof Player player) {
                        ClientCastingTracker.INSTANCE.setCasting(player, casting);
                    }
                } finally {
                    buf.release();
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(CastingEventS2CPacket.CHANNEL, (client, handler, buf, responseSender) -> {
            final int sourceId = buf.readVarInt();
            final ResourceLocation id = buf.readResourceLocation();
            buf.retain();

            client.submit(() -> {
                try {
                    final Entity source = client.level.getEntity(sourceId);
                    if (source instanceof Player player) {
                        ClientCastingTracker.INSTANCE.handleEvent(player, id, buf);
                    }
                } finally {
                    buf.release();
                }
            });
        });
    }
}
