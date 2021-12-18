package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.glyph.GlyphType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class FinishGlyphS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("finish_glyph");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int networkId = buf.readVarInt();
            GlyphType type = GlyphType.REGISTRY.get(buf.readVarInt());
            if (type != null) {
                client.submit(() -> ClientSpellcastingTracker.INSTANCE.finishDrawingOwnGlyph(networkId, type));
            }
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph, GlyphType type) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(glyph.networkId());
        buf.writeVarInt(GlyphType.REGISTRY.getRawId(type));
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
