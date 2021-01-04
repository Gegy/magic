package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.GlyphPlane;
import dev.gegy.magic.glyph.ServerGlyphTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class CancelGlyphC2SPacket {
    private static final Identifier CHANNEL = Magic.identifier("cancel_glyph");

    static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            server.submit(() -> {
                ServerGlyphTracker.INSTANCE.cancelDrawingGlyph(player);
            });
        });
    }

    public static void sendToServer() {
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(CHANNEL, buf);
    }
}
