package dev.gegy.magic.client.casting;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;

public final record ConfiguredClientCasting<P>(ClientCastingType<P> type, P parameters) {
    public static final PacketCodec<ConfiguredClientCasting<?>> CODEC = PacketCodec.of(
            ConfiguredClientCasting::encode,
            ConfiguredClientCasting::decode
    );

    public ClientCasting build(PlayerEntity player, ClientCastingBuilder casting) {
        return this.type.factory().build(player, this.parameters, casting);
    }

    private void encode(PacketByteBuf buf) {
        ClientCastingType.PACKET_CODEC.encode(this.type, buf);
        this.type.parametersCodec().encode(this.parameters, buf);
    }

    private static ConfiguredClientCasting<?> decode(PacketByteBuf buf) {
        var type = ClientCastingType.PACKET_CODEC.decode(buf);
        return decodeTyped(type, buf);
    }

    private static <P> ConfiguredClientCasting<P> decodeTyped(ClientCastingType<P> type, PacketByteBuf buf) {
        var parameters = type.parametersCodec().decode(buf);
        return new ConfiguredClientCasting<>(type, parameters);
    }
}
