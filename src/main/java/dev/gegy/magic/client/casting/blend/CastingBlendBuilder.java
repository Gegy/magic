package dev.gegy.magic.client.casting.blend;

import dev.gegy.magic.client.casting.ClientCasting;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectMap;
import dev.gegy.magic.client.effect.EffectSelector;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public final class CastingBlendBuilder {
    private final EffectMap effects = new EffectMap();
    private final List<Ticker> tickers = new ArrayList<>();

    public <E extends Effect> E attachEffect(E effect) {
        this.effects.add(effect);
        return effect;
    }

    public void registerTicker(Ticker ticker) {
        this.tickers.add(ticker);
    }

    public void registerTicker(Runnable ticker) {
        this.registerTicker(() -> {
            ticker.run();
            return false;
        });
    }

    public ClientCasting build(ClientCasting target) {
        if (!this.isEmpty()) {
            return new BlendingCasting(target, this.effects, this.tickers);
        } else {
            return target;
        }
    }

    public boolean isEmpty() {
        return this.effects.isEmpty() && this.tickers.isEmpty();
    }

    public interface Ticker {
        boolean tick();
    }

    private static final class BlendingCasting implements ClientCasting {
        private ClientCasting target;
        private final EffectMap effects;
        private final List<Ticker> tickers;

        BlendingCasting(ClientCasting target, EffectMap effects, List<Ticker> tickers) {
            this.target = target;
            this.effects = effects;
            this.tickers = tickers;
        }

        @Override
        public ClientCasting handleServerCast(ClientCasting casting) {
            this.target = casting;
            return this;
        }

        @Override
        public ClientCasting tick() {
            for (var ticker : this.tickers) {
                if (ticker.tick()) {
                    return this.target;
                }
            }
            return this;
        }

        @Override
        public void handleEvent(Identifier id, PacketByteBuf buf) {
            var target = this.target;
            if (target != null) {
                target.handleEvent(id, buf);
            }
        }

        @Override
        public EffectSelector getEffects() {
            return this.effects;
        }
    }
}
