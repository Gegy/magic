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
        return selector;
    }

    @Override
    public boolean add(final Effect effect) {
        getEffectsLike(effect).add(effect);
        return true;
    }

    @Override
    public boolean remove(final Object o) {
        return o instanceof Effect effect && remove(effect);
    }

    private boolean remove(final Effect effect) {
        return getEffectsLike(effect).remove(effect);
    }

    @Override
    public Iterator<Effect> iterator() {
        return Iterables.concat(effectsByType.values()).iterator();
    }

    @Override
    public int size() {
        int size = 0;
        for (final List<Effect> effects : effectsByType.values()) {
            size += effects.size();
        }
        return size;
    }

    @SuppressWarnings("unchecked")
    private <E extends Effect> List<E> getEffectsLike(final E effect) {
        return (List<E>) getEffectsByType(effect.getType());
    }

    @SuppressWarnings("unchecked")
    private <E extends Effect> List<E> getEffectsByType(final EffectType<E> type) {
        return (List<E>) effectsByType.computeIfAbsent(type, t -> new ArrayList<>());
    }

    private final class Selector implements EffectSelector {
        @Override
        public <E extends Effect> Selection<E> select(final EffectType<E> type) {
            final List<E> effects = getEffectsByType(type);
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
