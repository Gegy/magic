package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.spell.Spell;
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
            Spell spell = Spell.REGISTRY.get(buf.readVarInt());
            if (spell != null) {
                client.submit(() -> ClientGlyphTracker.INSTANCE.finishDrawingOwnGlyph(networkId, spell));
            }
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph, Spell spell) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(glyph.getNetworkId());
        buf.writeVarInt(Spell.REGISTRY.getRawId(spell));
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
