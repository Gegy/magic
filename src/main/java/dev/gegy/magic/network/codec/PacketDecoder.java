package dev.gegy.magic.network.codec;

import net.minecraft.network.FriendlyByteBuf;

public interface PacketDecoder<T> {
    T decode(FriendlyByteBuf buf);
}
