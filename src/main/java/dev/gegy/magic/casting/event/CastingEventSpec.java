package dev.gegy.magic.casting.event;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.resources.ResourceLocation;

public record CastingEventSpec<T>(
        ResourceLocation id,
        PacketCodec<T> codec
) {
    public static <T> CastingEventSpec<T> of(ResourceLocation id, PacketCodec<T> codec) {
        return new CastingEventSpec<>(id, codec);
    }
}
