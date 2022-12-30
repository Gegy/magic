package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class SetCastingS2CPacket {
    static final ResourceLocation CHANNEL = Magic.identifier("set_casting");

    public static FriendlyByteBuf create(final Player source, @Nullable final ConfiguredClientCasting<?> casting) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(source.getId());
        ConfiguredClientCasting.CODEC.nullable().encode(casting, buf);
        return buf;
    }

    public static void sendTo(final ServerPlayer player, final FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
