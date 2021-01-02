package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.GlyphPlane;
import dev.gegy.magic.glyph.ServerGlyphTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class BeginGlyphC2SPacket {
    private static final Identifier CHANNEL = Magic.identifier("begin_glyph");

    static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            // TODO: any sort of validation
            GlyphPlane plane = GlyphPlane.readFrom(buf);
            float radius = buf.readFloat();
            server.submit(() -> {
                ServerGlyphTracker.INSTANCE.startDrawingGlyph(player, plane, radius);
            });
        });
    }

    public static void sendToServer(GlyphPlane plane, float radius) {
        PacketByteBuf buf = PacketByteBufs.create();

        plane.writeTo(buf);
        buf.writeFloat(radius);

        ClientPlayNetworking.send(CHANNEL, buf);
    }
}
