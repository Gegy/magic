package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class SetPreparedSpellS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("set_prepared_spell");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int sourceId = buf.readVarInt();
            boolean animate = buf.readBoolean();
            client.submit(() -> {
                Entity sourceEntity = handler.getWorld().getEntityById(sourceId);
                if (sourceEntity != null) {
                    ClientSpellcastingTracker.INSTANCE.prepareSpellFor(sourceEntity, animate);
                }
            });
        });
    }

    public static PacketByteBuf create(ServerPlayerEntity player, boolean animate) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(player.getId());
        buf.writeBoolean(animate);
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
