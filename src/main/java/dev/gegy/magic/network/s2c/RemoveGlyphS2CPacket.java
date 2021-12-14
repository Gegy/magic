package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class RemoveGlyphS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("remove_glyph");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int networkId = buf.readVarInt();
            client.submit(() -> {
                ClientSpellcastingTracker.INSTANCE.removeGlyph(networkId);
            });
        });
    }

    public static PacketByteBuf create(int networkId) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(networkId);
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
