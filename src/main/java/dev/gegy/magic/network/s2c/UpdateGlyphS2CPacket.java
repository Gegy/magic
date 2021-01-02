package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.glyph.ServerGlyph;
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
            // TODO: registry for spells
            Spell matchedSpell = buf.readBoolean() ? Spell.TEST : null;

            client.submit(() -> {
                ClientGlyph glyph = ClientGlyphTracker.INSTANCE.getGlyphById(networkId);
                if (glyph != null) {
                    glyph.shape = shape;
                    if (matchedSpell != null) {
                        glyph.applySpell(matchedSpell);
                    }
                }
            });
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(glyph.getNetworkId());
        buf.writeShort(glyph.getShape());
        buf.writeBoolean(glyph.getMatchedSpell() != null);
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}