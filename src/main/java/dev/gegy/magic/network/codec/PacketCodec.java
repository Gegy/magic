package dev.gegy.magic.network.codec;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PacketCodec<T> extends PacketEncoder<T>, PacketDecoder<T> {
    PacketCodec<Boolean> BOOLEAN = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value),
            FriendlyByteBuf::readBoolean
    );

    PacketCodec<java.util.UUID> UUID = PacketCodec.of(
            (uuid, buf) -> buf.writeUUID(uuid),
            FriendlyByteBuf::readUUID
    );

    PacketCodec<Vector3f> VEC3F = PacketCodec.of(
            (vec, buf) -> {
                buf.writeFloat(vec.x());
                buf.writeFloat(vec.y());
                buf.writeFloat(vec.z());
            },
            buf -> new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
    );

    PacketCodec<ChatFormatting> FORMATTING = PacketCodec.ofEnum(ChatFormatting.class);

    PacketCodec<TextColor> TEXT_COLOR = PacketCodec.of(
            (color, buf) -> buf.writeInt(color.getValue()),
            buf -> TextColor.fromRgb(buf.readInt())
    );

    static <T> PacketCodec<T> unit(final Supplier<T> supplier) {
        return PacketCodec.of((value, buf) -> {}, buf -> supplier.get());
    }

    static <T extends Enum<T>> PacketCodec<@Nullable T> ofEnum(final Class<T> enumClass) {
        final T[] values = enumClass.getEnumConstants();
        return PacketCodec.of(
                (value, buf) -> buf.writeByte(value != null ? value.ordinal() & 0xFF : Byte.MIN_VALUE),
                buf -> {
                    final int ordinal = buf.readUnsignedByte();
                    return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
                }
        );
    }

    static <T> PacketCodec<@Nullable T> ofRegistry(final IdMap<T> registry) {
        return PacketCodec.of(
                (value, buf) -> buf.writeId(registry, value),
                buf -> buf.readById(registry)
        );
    }

    static <K, V> PacketCodec<Map<K, V>> mapOf(final PacketCodec<K> keyCodec, final PacketCodec<V> valueCodec) {
        return new MapOf<>(keyCodec, valueCodec);
    }

    static <T> PacketCodec<T> of(final PacketEncoder<T> encoder, final PacketDecoder<T> decoder) {
        return new PacketCodec<>() {
            @Override
            public void encode(final T value, final FriendlyByteBuf buf) {
                encoder.encode(value, buf);
            }

            @Override
            public T decode(final FriendlyByteBuf buf) {
                return decoder.decode(buf);
            }
        };
    }

    @Override
    void encode(T value, FriendlyByteBuf buf);

    @Override
    T decode(FriendlyByteBuf buf);

    default PacketCodec<List<T>> list() {
        return new ListOf<>(this);
    }

    default PacketCodec<@Nullable T> nullable() {
        return new NullableOf<>(this);
    }

    default PacketCodec<T> orElse(final T defaultValue) {
        return PacketCodec.of(
                (value, buf) -> encode(value != null ? value : defaultValue, buf),
                buf -> Objects.requireNonNullElse(decode(buf), defaultValue)
        );
    }

    default <R> PacketCodec<R> map(
            final Function<T, R> mapTo,
            final Function<R, T> mapFrom
    ) {
        return new PacketCodec<>() {
            @Override
            public void encode(final R value, final FriendlyByteBuf buf) {
                PacketCodec.this.encode(mapFrom.apply(value), buf);
            }

            @Override
            public R decode(final FriendlyByteBuf buf) {
                return mapTo.apply(PacketCodec.this.decode(buf));
            }
        };
    }

    record MapOf<K, V>(PacketCodec<K> keyCodec, PacketCodec<V> valueCodec) implements PacketCodec<Map<K, V>> {
        @Override
        public void encode(final Map<K, V> map, final FriendlyByteBuf buf) {
            buf.writeVarInt(map.size());
            for (final Map.Entry<K, V> entry : map.entrySet()) {
                keyCodec.encode(entry.getKey(), buf);
                valueCodec.encode(entry.getValue(), buf);
            }
        }

        @Override
        public Map<K, V> decode(final FriendlyByteBuf buf) {
            final int count = buf.readVarInt();
            final Object2ObjectOpenHashMap<K, V> map = new Object2ObjectOpenHashMap<K, V>(count);
            for (int i = 0; i < count; i++) {
                final K key = keyCodec.decode(buf);
                final V value = valueCodec.decode(buf);
                map.put(key, value);
            }
            return map;
        }
    }

    record ListOf<T>(PacketCodec<T> elementCodec) implements PacketCodec<List<T>> {
        @Override
        public void encode(final List<T> list, final FriendlyByteBuf buf) {
            buf.writeVarInt(list.size());
            for (final T element : list) {
                elementCodec.encode(element, buf);
            }
        }

        @Override
        public List<T> decode(final FriendlyByteBuf buf) {
            final int size = buf.readVarInt();
            final List<T> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(elementCodec.decode(buf));
            }
            return list;
        }
    }

    record NullableOf<T>(PacketCodec<T> valueCodec) implements PacketCodec<@Nullable T> {
        @Override
        public void encode(final T value, final FriendlyByteBuf buf) {
            buf.writeBoolean(value != null);
            if (value != null) {
                valueCodec.encode(value, buf);
            }
        }

        @Override
        @Nullable
        public T decode(final FriendlyByteBuf buf) {
            return buf.readBoolean() ? valueCodec.decode(buf) : null;
        }
    }
}
