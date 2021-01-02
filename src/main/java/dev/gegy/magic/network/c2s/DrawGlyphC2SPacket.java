package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.ServerGlyphTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class DrawGlyphC2SPacket {
    private static final Identifier CHANNEL = Magic.identifier("draw_glyph");

    static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            int shape = buf.readShort();
            server.submit(() -> {
                ServerGlyphTracker.INSTANCE.updateDrawing(player, shape);
            });
        });
    }

    public static void sendToServer(int shape) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeShort(shape);

        ClientPlayNetworking.send(CHANNEL, buf);
    }
}
