package dev.gegy.magic.client.casting;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public record ConfiguredClientCasting<P>(ClientCastingType<P> type, P parameters) {
    public static final PacketCodec<ConfiguredClientCasting<?>> CODEC = PacketCodec.of(
            ConfiguredClientCasting::encode,
            ConfiguredClientCasting::decode
    );

    public ClientCasting build(final Player player, final ClientCastingBuilder casting) {
        return type.factory().build(player, parameters, casting);
    }

    private void encode(final FriendlyByteBuf buf) {
        ClientCastingType.PACKET_CODEC.encode(type, buf);
        type.parametersCodec().encode(parameters, buf);
    }

    private static ConfiguredClientCasting<?> decode(final FriendlyByteBuf buf) {
        final ClientCastingType<?> type = ClientCastingType.PACKET_CODEC.decode(buf);
        return decodeTyped(type, buf);
    }

    private static <P> ConfiguredClientCasting<P> decodeTyped(final ClientCastingType<P> type, final FriendlyByteBuf buf) {
        final P parameters = type.parametersCodec().decode(buf);
        return new ConfiguredClientCasting<>(type, parameters);
    }
}
