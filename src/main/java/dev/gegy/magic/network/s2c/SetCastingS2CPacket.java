package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.casting.ClientCastingTracker;
import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class SetCastingS2CPacket {
    private static final ResourceLocation CHANNEL = Magic.identifier("set_casting");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int sourceId = buf.readVarInt();
            var casting = ConfiguredClientCasting.CODEC.nullable().decode(buf);
            client.submit(() -> {
                try {
                    var source = client.level.getEntity(sourceId);
                    if (source instanceof Player player) {
                        ClientCastingTracker.INSTANCE.setCasting(player, casting);
                    }
                } finally {
                    buf.release();
                }
            });
        });
    }

    public static <T> FriendlyByteBuf create(Player source, @Nullable ConfiguredClientCasting<?> casting) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(source.getId());
        ConfiguredClientCasting.CODEC.nullable().encode(casting, buf);
        return buf;
    }

    public static void sendTo(ServerPlayer player, FriendlyByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
