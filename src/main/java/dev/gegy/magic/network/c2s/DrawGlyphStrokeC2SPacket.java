package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.ServerGlyphTracker;
import dev.gegy.magic.glyph.shape.GlyphNode;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public final class DrawGlyphStrokeC2SPacket {
    private static final Identifier CHANNEL = Magic.identifier("draw_glyph_stroke");

    static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            GlyphNode node = GlyphNode.byId(buf.readUnsignedByte());
            server.submit(() -> {
                ServerGlyphTracker.INSTANCE.updateDrawingStroke(player, node);
            });
        });
    }

    public static void sendStartToServer(GlyphNode node) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(node.ordinal() & 0xFF);

        ClientPlayNetworking.send(CHANNEL, buf);
    }

    public static void sendStopToServer() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeByte(0xFF);

        ClientPlayNetworking.send(CHANNEL, buf);
    }
}
