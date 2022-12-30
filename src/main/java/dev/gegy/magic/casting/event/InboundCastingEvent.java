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

    public InboundCastingEvent(final ResourceLocation id, final PacketDecoder<T> decoder) {
        this.id = id;
        this.decoder = decoder;
    }

    public InboundCastingEvent<T> addHandler(final Consumer<T> handler) {
        handlers.add(handler);
        return this;
    }

    public ResourceLocation id() {
        return id;
    }

    public T decode(final FriendlyByteBuf buf) {
        return decoder.decode(buf);
    }

    public void accept(final T event) {
        for (final Consumer<T> handler : handlers) {
            handler.accept(event);
        }
    }

    public void acceptBytes(final FriendlyByteBuf buf) {
        final T event = decoder.decode(buf);
        accept(event);
    }
}
