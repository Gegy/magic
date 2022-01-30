package dev.gegy.magic.client.casting.blend;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

public final class CastingBlender {
    public static final CastingBlender EMPTY = new CastingBlender();

    private final Reference2ObjectMap<CastingBlendType<?>, Entry<?>> entries = new Reference2ObjectOpenHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> Entry<T> entry(CastingBlendType<T> type) {
        return (Entry<T>) this.entries.computeIfAbsent(type, t -> new Entry<>());
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> Into<T> loadBlendInto(CastingBlendType<T> type) {
        var entry = (Entry<T>) this.entries.remove(type);
        return entry != null ? entry.blendInto : null;
    }

    public void loadBlendOut(CastingBlendBuilder blendBuilder) {
        for (var entry : this.entries.values()) {
            if (entry.blendOut != null) {
                entry.blendOut.accept(blendBuilder);
            }
        }
    }

    public static final class Entry<T> {
        private Into<T> blendInto;
        private Out blendOut;

        public Entry<T> blendInto(Into<T> blend) {
            this.blendInto = blend;
            return this;
        }

        public Entry<T> blendOut(Out blend) {
            this.blendOut = blend;
            return this;
        }
    }

    public interface Into<T> {
        T apply(CastingBlendBuilder blend);
    }

    public interface Out {
        void accept(CastingBlendBuilder blend);
    }
}
