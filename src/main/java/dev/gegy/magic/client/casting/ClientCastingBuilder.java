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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
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

    private final Map<ResourceLocation, InboundCastingEvent<?>> inboundEvents = new Object2ObjectOpenHashMap<>();

    private final List<Ticker> tickers = new ArrayList<>();
    private final EffectMap effects = new EffectMap();

    public ClientCastingBuilder(final EventSenderFactory senderFactory, final CastingBlender blenderIn) {
        this.senderFactory = senderFactory;
        this.blenderIn = blenderIn;
    }

    public <T> void bindInboundEvent(final CastingEventSpec<T> spec, final Consumer<T> handler) {
        getOrCreateInboundEvent(spec).addHandler(handler);
    }

    @SuppressWarnings("unchecked")
    private <T> InboundCastingEvent<T> getOrCreateInboundEvent(final CastingEventSpec<T> spec) {
        return (InboundCastingEvent<T>) inboundEvents.computeIfAbsent(
                spec.id(),
                id -> new InboundCastingEvent<>(id, spec.codec())
        );
    }

    public <T> NetworkSender<T> registerOutboundEvent(final CastingEventSpec<T> spec) {
        return senderFactory.create(spec);
    }

    public void registerTicker(final Ticker ticker) {
        tickers.add(ticker);
    }

    public <E extends Effect> E attachEffect(final E effect) {
        effects.add(effect);
        return effect;
    }

    public <T, P> CastingBlender.Entry<T, P> blendTo(final CastingBlendType<T, P> type) {
        return blenderOut.entry(type);
    }

    @Nullable
    public <T> T blendFrom(final CastingBlendType<T, Unit> type) {
        return blendFrom(type, Unit.INSTANCE);
    }

    @Nullable
    public <T, P> T blendFrom(final CastingBlendType<T, P> type, final P parameter) {
        final CastingBlender.Into<T, P> input = blenderIn.loadBlendInto(type);
        if (input != null) {
            return input.apply(blendBuilder, parameter);
        } else {
            return null;
        }
    }

    public ClientCasting build() {
        final Casting targetCasting = new Casting(inboundEvents, tickers, effects, blenderOut);

        blenderIn.loadBlendOut(blendBuilder);
        return blendBuilder.build(targetCasting);
    }

    public interface Ticker {
        void tick();
    }

    private record Casting(
            Map<ResourceLocation, InboundCastingEvent<?>> inboundEvents,
            List<Ticker> tickers,
            EffectMap effects,
            CastingBlender blenderOut
    ) implements ClientCasting {
        @Override
        public ClientCasting tick() {
            for (final Ticker ticker : tickers) {
                ticker.tick();
            }
            return this;
        }

        @Override
        public void handleEvent(final ResourceLocation id, final FriendlyByteBuf buf) {
            final InboundCastingEvent<?> event = inboundEvents.get(id);
            if (event != null) {
                event.acceptBytes(buf);
            } else {
                Magic.LOGGER.warn("Received inbound casting event with unknown id: '{}'", id);
            }
        }

        @Override
        public EffectSelector getEffects() {
            return effects;
        }

        @Override
        public CastingBlender getBlender() {
            return blenderOut;
        }
    }
}
