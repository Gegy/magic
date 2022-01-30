package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.client.casting.ClientCastingTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class CastingEventS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("casting_event");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int sourceId = buf.readVarInt();
            var id = buf.readIdentifier();
            buf.retain();

            client.submit(() -> {
                try {
                    var source = client.world.getEntityById(sourceId);
                    if (source instanceof PlayerEntity player) {
                        ClientCastingTracker.INSTANCE.handleEvent(player, id, buf);
                    }
                } finally {
                    buf.release();
                }
            });
        });
    }

    public static <T> PacketByteBuf create(PlayerEntity source, CastingEventSpec<T> spec, T event) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(source.getId());
        buf.writeIdentifier(spec.id());
        spec.codec().encode(event, buf);
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
