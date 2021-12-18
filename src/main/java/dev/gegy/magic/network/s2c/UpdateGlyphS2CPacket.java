package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.glyph.GlyphType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class UpdateGlyphS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("update_glyph");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int networkId = buf.readVarInt();
            int shape = buf.readShort();
            GlyphNode stroke = GlyphNode.byId(buf.readUnsignedByte());

            GlyphType matchedGlyphType = GlyphType.REGISTRY.get(buf.readVarInt());
            client.submit(() -> ClientSpellcastingTracker.INSTANCE.updateGlyph(networkId, shape, stroke, matchedGlyphType));
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(glyph.networkId());
        buf.writeShort(glyph.getShape());

        GlyphNode stroke = glyph.getStroke();
        int strokeId = stroke != null ? stroke.ordinal() : 0xFF;
        buf.writeByte(strokeId & 0xFF);

        GlyphType matchedGlyphType = glyph.getMatchedType();
        buf.writeVarInt(matchedGlyphType != null ? GlyphType.REGISTRY.getRawId(matchedGlyphType) : -1);

        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
