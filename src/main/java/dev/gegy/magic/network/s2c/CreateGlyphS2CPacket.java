package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.transform.GlyphPlane;
import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.glyph.GlyphType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;

public final class CreateGlyphS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("create_glyph");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int networkId = buf.readVarInt();
            int sourceId = buf.readVarInt();
            float directionX = buf.readFloat();
            float directionY = buf.readFloat();
            float directionZ = buf.readFloat();
            GlyphPlane plane = GlyphPlane.create(new Vec3f(directionX, directionY, directionZ), GlyphPlane.DRAW_DISTANCE);
            float radius = buf.readFloat();
            int shape = buf.readShort();
            GlyphType matchedGlyphType = GlyphType.REGISTRY.get(buf.readVarInt());

            client.submit(() -> {
                ClientWorld world = handler.getWorld();
                Entity sourceEntity = world.getEntityById(sourceId);
                if (sourceEntity != null) {
                    ClientGlyph glyph = ClientSpellcastingTracker.INSTANCE.addGlyph(networkId, sourceEntity, plane, radius, shape);
                    if (matchedGlyphType != null) {
                        glyph.applyMatchedType(matchedGlyphType);
                    }
                }
            });
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph) {
        PacketByteBuf buf = PacketByteBufs.create();

        buf.writeVarInt(glyph.networkId());
        buf.writeVarInt(glyph.source().getPlayer().getId());

        Vec3f direction = glyph.direction();
        buf.writeFloat(direction.getX());
        buf.writeFloat(direction.getY());
        buf.writeFloat(direction.getZ());

        buf.writeFloat(glyph.radius());
        buf.writeShort(glyph.getShape());

        GlyphType matchedGlyphType = glyph.getMatchedType();
        buf.writeVarInt(matchedGlyphType != null ? GlyphType.REGISTRY.getRawId(matchedGlyphType) : -1);

        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
