package dev.gegy.magic.client.effect;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;

public final class EffectMap extends AbstractCollection<Effect> implements EffectSelector {
    private final Map<EffectType<?>, Effect> effects = new Reference2ObjectOpenHashMap<>();

    @Override
    public boolean add(final Effect effect) {
        return effects.putIfAbsent(effect.getType(), effect) == null;
    }

    @Override
    public boolean remove(final Object o) {
        return o instanceof Effect effect && remove(effect);
    }

    public boolean remove(final Effect effect) {
        return effects.remove(effect.getType(), effect);
    }

    @Override
    public void clear() {
        effects.clear();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <E extends Effect> E get(final EffectType<E> type) {
        return (E) effects.get(type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <E extends Effect> E remove(final EffectType<E> type) {
        return (E) effects.remove(type);
    }

    @Override
    public boolean contains(final Object o) {
        return o instanceof Effect effect && contains(effect);
    }

    public boolean contains(final Effect effect) {
        return effects.get(effect.getType()) == effect;
    }

    public boolean contains(final EffectType<?> type) {
        return effects.containsKey(type);
    }

    @Override
    public <E extends Effect> Selection<E> select(final EffectType<E> type) {
        final E effect = get(type);
        if (effect != null) {
            return Selection.singleton(effect);
        } else {
            return Selection.empty();
        }
    }

    @Override
    @Nullable
    public <E extends Effect> E selectAny(final EffectType<E> type) {
        return get(type);
    }

    @NotNull
    @Override
    public Iterator<Effect> iterator() {
        return effects.values().iterator();
    }

    @Override
    public int size() {
        return effects.size();
    }
}
