package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.glyph.GlyphPlane;
import dev.gegy.magic.glyph.ServerGlyph;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class CreateGlyphS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("create_glyph");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int networkId = buf.readVarInt();
            int sourceId = buf.readVarInt();
            GlyphPlane plane = GlyphPlane.readFrom(buf);
            float radius = buf.readFloat();
            int shape = buf.readInt();

            client.submit(() -> {
                ClientWorld world = handler.getWorld();
                Entity sourceEntity = world.getEntityById(sourceId);
                if (sourceEntity != null) {
                    ClientGlyphTracker.INSTANCE.addGlyph(networkId, sourceEntity, plane, radius, shape);
                }
            });
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(glyph.getNetworkId());
        buf.writeVarInt(glyph.getSource().getEntityId());
        glyph.getPlane().writeTo(buf);
        buf.writeFloat(glyph.getRadius());
        buf.writeInt(glyph.getShape());

        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
