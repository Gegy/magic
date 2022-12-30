package dev.gegy.magic.client.casting.blend;

import dev.gegy.magic.client.casting.ClientCasting;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectMap;
import dev.gegy.magic.client.effect.EffectSelector;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class CastingBlendBuilder {
    private final EffectMap effects = new EffectMap();
    private final List<Ticker> tickers = new ArrayList<>();

    public <E extends Effect> E attachEffect(final E effect) {
        effects.add(effect);
        return effect;
    }

    public void registerTicker(final Ticker ticker) {
        tickers.add(ticker);
    }

    public void registerTicker(final Runnable ticker) {
        registerTicker(() -> {
            ticker.run();
            return false;
        });
    }

    public ClientCasting build(final ClientCasting target) {
        if (!isEmpty()) {
            return new BlendingCasting(target, effects, tickers);
        } else {
            return target;
        }
    }

    public boolean isEmpty() {
        return effects.isEmpty() && tickers.isEmpty();
    }

    public interface Ticker {
        boolean tick();
    }

    private static final class BlendingCasting implements ClientCasting {
        private ClientCasting target;
        private final EffectMap effects;
        private final List<Ticker> tickers;

        BlendingCasting(final ClientCasting target, final EffectMap effects, final List<Ticker> tickers) {
            this.target = target;
            this.effects = effects;
            this.tickers = tickers;
        }

        @Override
        public ClientCasting handleServerCast(final ClientCasting casting) {
            target = casting;
            return this;
        }

        @Override
        public ClientCasting tick() {
            for (final Ticker ticker : tickers) {
                if (ticker.tick()) {
                    return target;
                }
            }
            return this;
        }

        @Override
        public void handleEvent(final ResourceLocation id, final FriendlyByteBuf buf) {
            final ClientCasting target = this.target;
            if (target != null) {
                target.handleEvent(id, buf);
            }
        }

        @Override
        public EffectSelector getEffects() {
            return effects;
        }
    }
}
