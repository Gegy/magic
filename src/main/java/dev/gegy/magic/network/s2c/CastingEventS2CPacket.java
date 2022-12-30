package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class CastingEventS2CPacket {
    static final ResourceLocation CHANNEL = Magic.identifier("casting_event");

    public static <T> FriendlyByteBuf create(final Player source, final CastingEventSpec<T> spec, final T event) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(source.getId());
        buf.writeResourceLocation(spec.id());
        spec.codec().encode(event, buf);
        return buf;
    }

    public static void sendTo(final ServerPlayer player, final FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
