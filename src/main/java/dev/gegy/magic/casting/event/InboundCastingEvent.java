package dev.gegy.magic.casting.event;

import dev.gegy.magic.network.codec.PacketDecoder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class InboundCastingEvent<T> {
    private final ResourceLocation id;
    private final PacketDecoder<T> decoder;
    private final List<Consumer<T>> handlers = new ArrayList<>();

    public InboundCastingEvent(ResourceLocation id, PacketDecoder<T> decoder) {
        this.id = id;
        this.decoder = decoder;
    }

    public InboundCastingEvent<T> addHandler(Consumer<T> handler) {
        this.handlers.add(handler);
        return this;
    }

    public ResourceLocation id() {
        return this.id;
    }

    public T decode(FriendlyByteBuf buf) {
        return this.decoder.decode(buf);
    }

    public void accept(T event) {
        for (var handler : this.handlers) {
            handler.accept(event);
        }
    }

    public void acceptBytes(FriendlyByteBuf buf) {
        var event = this.decoder.decode(buf);
        this.accept(event);
    }
}
