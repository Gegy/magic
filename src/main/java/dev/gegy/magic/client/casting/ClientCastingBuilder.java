package dev.gegy.magic.client.casting;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.casting.event.EventSenderFactory;
import dev.gegy.magic.casting.event.InboundCastingEvent;
import dev.gegy.magic.client.casting.blend.CastingBlendBuilder;
import dev.gegy.magic.client.casting.blend.CastingBlendType;
import dev.gegy.magic.client.casting.blend.CastingBlender;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectMap;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.network.NetworkSender;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class ClientCastingBuilder {
    private final EventSenderFactory senderFactory;

    private final CastingBlender blenderIn;
    private final CastingBlender blenderOut = new CastingBlender();

    private final CastingBlendBuilder blendBuilder = new CastingBlendBuilder();

    private final Map<Identifier, InboundCastingEvent<?>> inboundEvents = new Object2ObjectOpenHashMap<>();

    private final List<Ticker> tickers = new ArrayList<>();
    private final EffectMap effects = new EffectMap();

    public ClientCastingBuilder(EventSenderFactory senderFactory, CastingBlender blenderIn) {
        this.senderFactory = senderFactory;
        this.blenderIn = blenderIn;
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

    public <E extends Effect> E attachEffect(E effect) {
        this.effects.add(effect);
        return effect;
    }

    public <T> CastingBlender.Entry<T> blendTo(CastingBlendType<T> type) {
        return this.blenderOut.entry(type);
    }

    @Nullable
    public <T> T blendFrom(CastingBlendType<T> type) {
        var input = this.blenderIn.loadBlendInto(type);
        if (input != null) {
            return input.apply(this.blendBuilder);
        } else {
            return null;
        }
    }

    public ClientCasting build() {
        var targetCasting = new Casting(this.inboundEvents, this.tickers, this.effects, this.blenderOut);

        this.blenderIn.loadBlendOut(this.blendBuilder);
        return this.blendBuilder.build(targetCasting);
    }

    public interface Ticker {
        void tick();
    }

    private record Casting(
            Map<Identifier, InboundCastingEvent<?>> inboundEvents,
            List<Ticker> tickers,
            EffectMap effects,
            CastingBlender blenderOut
    ) implements ClientCasting {
        @Override
        public ClientCasting tick() {
            for (var ticker : this.tickers) {
                ticker.tick();
            }
            return this;
        }

        @Override
        public void handleEvent(Identifier id, PacketByteBuf buf) {
            var event = this.inboundEvents.get(id);
            if (event != null) {
                event.acceptBytes(buf);
            } else {
                Magic.LOGGER.warn("Received inbound casting event with unknown id: '{}'", id);
            }
        }

        @Override
        public EffectSelector getEffects() {
            return this.effects;
        }

        @Override
        public CastingBlender getBlender() {
            return this.blenderOut;
        }
    }
}
