package dev.gegy.magic.casting.event;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record CastingEventSpec<T>(
        Identifier id,
        PacketCodec<T> codec
) {
    public static <T> CastingEventSpec<T> of(Identifier id, PacketCodec<T> codec) {
        return new CastingEventSpec<>(id, codec);
    }
}
