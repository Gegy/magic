package dev.gegy.magic.client.casting.blend;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class CastingBlender {
    public static final CastingBlender EMPTY = new CastingBlender();

    private final Reference2ObjectMap<CastingBlendType<?, ?>, Entry<?, ?>> entries = new Reference2ObjectOpenHashMap<>();

    @SuppressWarnings("unchecked")
    public <T, P> Entry<T, P> entry(CastingBlendType<T, P> type) {
        return (Entry<T, P>) this.entries.computeIfAbsent(type, t -> new Entry<>());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T, P> Into<T, P> loadBlendInto(CastingBlendType<T, P> type) {
        var entry = (Entry<T, P>) this.entries.remove(type);
        return entry != null ? entry.blendInto : null;
    }

    public void loadBlendOut(CastingBlendBuilder blendBuilder) {
        for (var entry : this.entries.values()) {
            if (entry.blendOut != null) {
                entry.blendOut.accept(blendBuilder);
            }
        }
    }

    public static final class Entry<T, P> {
        private Into<T, P> blendInto;
        private Out blendOut;

        public Entry<T, P> blendInto(Into<T, P> blend) {
            this.blendInto = blend;
            return this;
        }

        public Entry<T, P> blendInto(Function<CastingBlendBuilder, T> blend) {
            return this.blendInto((builder, parameter) -> blend.apply(builder));
        }

        public Entry<T, P> blendOut(Out blend) {
            this.blendOut = blend;
            return this;
        }
    }

    public interface Into<T, P> {
        T apply(CastingBlendBuilder blend, P parameter);
    }

    public interface Out {
        void accept(CastingBlendBuilder blend);
    }
}
