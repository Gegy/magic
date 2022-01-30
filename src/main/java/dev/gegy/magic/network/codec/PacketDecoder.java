package dev.gegy.magic.network.codec;

import net.minecraft.network.PacketByteBuf;

public interface PacketDecoder<T> {
    T decode(PacketByteBuf buf);
}
