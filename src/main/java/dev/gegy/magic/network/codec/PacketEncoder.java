package dev.gegy.magic.network.codec;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;

public interface PacketEncoder<T> {
    void encode(T value, PacketByteBuf buf);

    default PacketByteBuf encodeStart(T value) {
        var buf = PacketByteBufs.create();
        this.encode(value, buf);
        return buf;
    }
}
