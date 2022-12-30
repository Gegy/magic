package dev.gegy.magic.client.casting;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public final record ConfiguredClientCasting<P>(ClientCastingType<P> type, P parameters) {
    public static final PacketCodec<ConfiguredClientCasting<?>> CODEC = PacketCodec.of(
            ConfiguredClientCasting::encode,
            ConfiguredClientCasting::decode
    );

    public ClientCasting build(Player player, ClientCastingBuilder casting) {
        return this.type.factory().build(player, this.parameters, casting);
    }

    private void encode(FriendlyByteBuf buf) {
        ClientCastingType.PACKET_CODEC.encode(this.type, buf);
        this.type.parametersCodec().encode(this.parameters, buf);
    }

    private static ConfiguredClientCasting<?> decode(FriendlyByteBuf buf) {
        var type = ClientCastingType.PACKET_CODEC.decode(buf);
        return decodeTyped(type, buf);
    }

    private static <P> ConfiguredClientCasting<P> decodeTyped(ClientCastingType<P> type, FriendlyByteBuf buf) {
        var parameters = type.parametersCodec().decode(buf);
        return new ConfiguredClientCasting<>(type, parameters);
    }
}
