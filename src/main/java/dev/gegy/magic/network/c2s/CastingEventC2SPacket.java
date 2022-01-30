package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.ServerCastingTracker;
import dev.gegy.magic.casting.event.CastingEventSpec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class CastingEventC2SPacket {
    private static final Identifier CHANNEL = Magic.identifier("casting_event");

    static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            var id = buf.readIdentifier();
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

    public static <T> void sendToServer(CastingEventSpec<T> spec, T event) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(spec.id());
        spec.codec().encode(event, buf);

        ClientPlayNetworking.send(CHANNEL, buf);
    }
}