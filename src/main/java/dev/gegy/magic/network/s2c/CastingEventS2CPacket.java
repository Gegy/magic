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
import net.minecraft.world.entity.player.Player;

public final class CastingEventS2CPacket {
    private static final ResourceLocation CHANNEL = Magic.identifier("casting_event");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int sourceId = buf.readVarInt();
            var id = buf.readResourceLocation();
            buf.retain();

            client.submit(() -> {
                try {
                    var source = client.level.getEntity(sourceId);
                    if (source instanceof Player player) {
                        ClientCastingTracker.INSTANCE.handleEvent(player, id, buf);
                    }
                } finally {
                    buf.release();
                }
            });
        });
    }

    public static <T> FriendlyByteBuf create(Player source, CastingEventSpec<T> spec, T event) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(source.getId());
        buf.writeResourceLocation(spec.id());
        spec.codec().encode(event, buf);
        return buf;
    }

    public static void sendTo(ServerPlayer player, FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
