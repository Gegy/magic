package dev.gegy.magic.network.codec;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;

public interface PacketEncoder<T> {
    void encode(T value, FriendlyByteBuf buf);

    default FriendlyByteBuf encodeStart(T value) {
        var buf = PacketByteBufs.create();
        this.encode(value, buf);
        return buf;
    }
}
