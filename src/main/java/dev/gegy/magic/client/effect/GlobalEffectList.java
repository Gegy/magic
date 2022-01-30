package dev.gegy.magic.client.effect;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GlobalEffectList extends AbstractCollection<Effect> {
    private final Map<EffectType<?>, List<Effect>> effectsByType = new Reference2ObjectOpenHashMap<>();

    private final Selector selector = new Selector();

    public EffectSelector selector() {
        return this.selector;
    }

    @Override
    public boolean add(Effect effect) {
        this.getEffectsLike(effect).add(effect);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Effect effect && this.remove(effect);
    }

    private boolean remove(Effect effect) {
        return this.getEffectsLike(effect).remove(effect);
    }

    @Override
    public Iterator<Effect> iterator() {
        return Iterables.concat(this.effectsByType.values()).iterator();
    }

    @Override
    public int size() {
        int size = 0;
        for (var effects : this.effectsByType.values()) {
            size += effects.size();
        }
        return size;
    }

    @SuppressWarnings("unchecked")
    private <E extends Effect> List<E> getEffectsLike(E effect) {
        return (List<E>) this.getEffectsByType(effect.getType());
    }

    @SuppressWarnings("unchecked")
    private <E extends Effect> List<E> getEffectsByType(EffectType<E> type) {
        return (List<E>) this.effectsByType.computeIfAbsent(type, t -> new ArrayList<>());
    }

    private final class Selector implements EffectSelector {
        @Override
        public <E extends Effect> Selection<E> select(EffectType<E> type) {
            var effects = GlobalEffectList.this.getEffectsByType(type);
            return new Selection<>() {
                @Override
                public boolean isEmpty() {
                    return effects.isEmpty();
                }

                @Override
                public Iterator<E> iterator() {
                    return effects.iterator();
                }
            };
        }

        @Override
        public Iterator<Effect> iterator() {
            return GlobalEffectList.this.iterator();
        }
    }
}
