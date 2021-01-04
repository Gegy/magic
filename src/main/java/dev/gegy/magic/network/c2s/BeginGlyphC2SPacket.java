package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.ServerGlyphTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class BeginGlyphC2SPacket {
    private static final Identifier CHANNEL = Magic.identifier("begin_glyph");

    static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            // TODO: any sort of validation
            Vector3f direction = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
            float radius = buf.readFloat();
            server.submit(() -> {
                ServerGlyphTracker.INSTANCE.startDrawing(player, direction, radius);
            });
        });
    }

    public static void sendToServer(Vector3f direction, float radius) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeFloat(direction.getX());
        buf.writeFloat(direction.getY());
        buf.writeFloat(direction.getZ());
        buf.writeFloat(radius);

        ClientPlayNetworking.send(CHANNEL, buf);
    }
}
