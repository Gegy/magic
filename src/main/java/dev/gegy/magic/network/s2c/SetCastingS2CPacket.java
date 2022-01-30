package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.casting.ClientCastingTracker;
import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public final class SetCastingS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("set_casting");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            int sourceId = buf.readVarInt();
            var casting = ConfiguredClientCasting.CODEC.nullable().decode(buf);
            client.submit(() -> {
                try {
                    var source = client.world.getEntityById(sourceId);
                    if (source instanceof PlayerEntity player) {
                        ClientCastingTracker.INSTANCE.setCasting(player, casting);
                    }
                } finally {
                    buf.release();
                }
            });
        });
    }

    public static <T> PacketByteBuf create(PlayerEntity source, @Nullable ConfiguredClientCasting<?> casting) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(source.getId());
        ConfiguredClientCasting.CODEC.nullable().encode(casting, buf);
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
