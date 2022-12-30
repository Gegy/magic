package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.client.casting.ClientCastingTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class CastingEventS2CPacket {
    private static final ResourceLocation CHANNEL = Magic.identifier("casting_event");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
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

    public static <T> FriendlyByteBuf create(final Player source, final CastingEventSpec<T> spec, final T event) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(source.getId());
        buf.writeResourceLocation(spec.id());
        spec.codec().encode(event, buf);
        return buf;
    }

    public static void sendTo(final ServerPlayer player, final FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
