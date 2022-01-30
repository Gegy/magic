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
    public boolean add(Effect effect) {
        return this.effects.putIfAbsent(effect.getType(), effect) == null;
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Effect effect && this.remove(effect);
    }

    public boolean remove(Effect effect) {
        return this.effects.remove(effect.getType(), effect);
    }

    @Override
    public void clear() {
        this.effects.clear();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <E extends Effect> E get(EffectType<E> type) {
        return (E) this.effects.get(type);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <E extends Effect> E remove(EffectType<E> type) {
        return (E) this.effects.remove(type);
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Effect effect && this.contains(effect);
    }

    public boolean contains(Effect effect) {
        return this.effects.get(effect.getType()) == effect;
    }

    public boolean contains(EffectType<?> type) {
        return this.effects.containsKey(type);
    }

    @Override
    public <E extends Effect> Selection<E> select(EffectType<E> type) {
        var effect = this.get(type);
        if (effect != null) {
            return Selection.singleton(effect);
        } else {
            return Selection.empty();
        }
    }

    @Override
    @Nullable
    public <E extends Effect> E selectAny(EffectType<E> type) {
        return this.get(type);
    }

    @NotNull
    @Override
    public Iterator<Effect> iterator() {
        return this.effects.values().iterator();
    }

    @Override
    public int size() {
        return this.effects.size();
    }
}
