package dev.gegy.magic.client.effect;

import com.google.common.collect.Iterators;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Iterator;

public interface EffectSelector extends Iterable<Effect> {
    EffectSelector EMPTY = new EffectSelector() {
        @Override
        public <E extends Effect> Selection<E> select(final EffectType<E> type) {
            return Selection.empty();
        }

        @Override
        public Iterator<Effect> iterator() {
            return Collections.emptyIterator();
        }
    };

    <E extends Effect> Selection<E> select(EffectType<E> type);

    @Nullable
    default <E extends Effect> E selectAny(final EffectType<E> type) {
        final Selection<E> selection = select(type);
        final Iterator<E> iterator = selection.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }

    default boolean has(final EffectType<?> type) {
        return !select(type).isEmpty();
    }

    @Override
    Iterator<Effect> iterator();

    interface Selection<E extends Effect> extends Iterable<E> {
        static <E extends Effect> Selection<E> singleton(final E effect) {
            return new Selection<>() {
                @Override
                public boolean isEmpty() {
                    return false;
                }

                @Override
                public Iterator<E> iterator() {
                    return Iterators.singletonIterator(effect);
                }
            };
        }

        static <E extends Effect> Selection<E> empty() {
            return new Selection<>() {
                @Override
                public boolean isEmpty() {
                    return true;
                }

                @Override
                public Iterator<E> iterator() {
                    return Collections.emptyIterator();
                }
            };
        }

        boolean isEmpty();

        @Override
        Iterator<E> iterator();
    }
}
