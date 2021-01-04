package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.GlyphPlane;
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
            float directionX = buf.readFloat();
            float directionY = buf.readFloat();
            float directionZ = buf.readFloat();
            float radius = buf.readFloat();

            GlyphPlane plane = GlyphPlane.create(directionX, directionY, directionZ, GlyphPlane.DRAW_DISTANCE);
            server.submit(() -> {
                ServerGlyphTracker.INSTANCE.startDrawing(player, plane, radius);
            });
        });
    }

    public static void sendToServer(GlyphPlane plane, float radius) {
        PacketByteBuf buf = PacketByteBufs.create();

        Vector3f direction = plane.getDirection();
        buf.writeFloat(direction.getX());
        buf.writeFloat(direction.getY());
        buf.writeFloat(direction.getZ());
        buf.writeFloat(radius);

        ClientPlayNetworking.send(CHANNEL, buf);
    }
}
