package dev.gegy.magic.casting;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.casting.event.EventSenderFactory;
import dev.gegy.magic.casting.event.InboundCastingEvent;
import dev.gegy.magic.client.casting.ClientCastingType;
import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import dev.gegy.magic.network.NetworkSender;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

// TODO: identifier ids are expensive over the network - can we switch to something that is more lightweight?
public final class ServerCastingBuilder {
    private final EventSenderFactory senderFactory;

    private final Map<ResourceLocation, InboundCastingEvent<?>> inboundEvents = new Object2ObjectOpenHashMap<>();
    private final List<Ticker> tickers = new ArrayList<>();

    private Supplier<@Nullable ConfiguredClientCasting<?>> clientCasting = () -> null;

    public ServerCastingBuilder(EventSenderFactory senderFactory) {
        this.senderFactory = senderFactory;
    }

    public <T> void bindInboundEvent(CastingEventSpec<T> spec, Consumer<T> handler) {
        this.getOrCreateInboundEvent(spec).addHandler(handler);
    }

    @SuppressWarnings("unchecked")
    private <T> InboundCastingEvent<T> getOrCreateInboundEvent(CastingEventSpec<T> spec) {
        return (InboundCastingEvent<T>) this.inboundEvents.computeIfAbsent(
                spec.id(),
                id -> new InboundCastingEvent<>(id, spec.codec())
        );
    }

    public <T> NetworkSender<T> registerOutboundEvent(CastingEventSpec<T> spec) {
        return this.senderFactory.create(spec);
    }

    public void registerTicker(Ticker ticker) {
        this.tickers.add(ticker);
    }

    public void registerClientCasting(Supplier<ConfiguredClientCasting<?>> casting) {
        this.clientCasting = casting;
    }

    public <P> void registerClientCasting(ClientCastingType<P> type, Supplier<P> parameters) {
        this.registerClientCasting(() -> type.configure(parameters.get()));
    }

    public ServerCasting build() {
        return new CastingImpl(this.inboundEvents, this.tickers, this.clientCasting);
    }

    public interface Ticker {
        @Nullable
        ServerCasting.Factory tick();
    }

    private static class CastingImpl implements ServerCasting {
        private final Map<ResourceLocation, InboundCastingEvent<?>> inboundEvents;
        private final List<Ticker> tickers;
        private final Supplier<ConfiguredClientCasting<?>> clientCasting;

        private CastingImpl(Map<ResourceLocation, InboundCastingEvent<?>> inboundEvents, List<Ticker> tickers, Supplier<ConfiguredClientCasting<?>> clientCasting) {
            this.inboundEvents = inboundEvents;
            this.tickers = tickers;
            this.clientCasting = clientCasting;
        }

        @Override
        @Nullable
        public ServerCasting.Factory tick() {
            for (var ticker : this.tickers) {
                var next = ticker.tick();
                if (next != null) {
                    return next;
                }
            }
            return null;
        }

        @Override
        public void handleEvent(ResourceLocation id, FriendlyByteBuf buf) {
            var event = this.inboundEvents.get(id);
            if (event != null) {
                event.acceptBytes(buf);
            } else {
                Magic.LOGGER.warn("Received inbound casting event with unknown id: '{}'", id);
            }
        }

        @Override
        @Nullable
        public ConfiguredClientCasting<?> createClientCasting() {
            return this.clientCasting.get();
        }
    }
}
