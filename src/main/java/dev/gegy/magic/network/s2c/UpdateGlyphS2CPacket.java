package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.spell.Spell;
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

            Spell matchedSpell = Spell.REGISTRY.get(buf.readVarInt());
            client.submit(() -> ClientGlyphTracker.INSTANCE.updateGlyph(networkId, shape, stroke, matchedSpell));
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(glyph.getNetworkId());
        buf.writeShort(glyph.getShape());

        GlyphNode stroke = glyph.getStroke();
        int strokeId = stroke != null ? stroke.ordinal() : 0xFF;
        buf.writeByte(strokeId & 0xFF);

        Spell matchedSpell = glyph.getMatchedSpell();
        buf.writeVarInt(matchedSpell != null ? Spell.REGISTRY.getRawId(matchedSpell) : -1);

        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
