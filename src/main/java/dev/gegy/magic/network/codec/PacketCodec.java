package dev.gegy.magic.network.codec;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PacketCodec<T> extends PacketEncoder<T>, PacketDecoder<T> {
    PacketCodec<Boolean> BOOLEAN = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value),
            PacketByteBuf::readBoolean
    );

    PacketCodec<java.util.UUID> UUID = PacketCodec.of(
            (uuid, buf) -> buf.writeUuid(uuid),
            PacketByteBuf::readUuid
    );

    PacketCodec<Vec3f> VEC3F = PacketCodec.of(
            (vec, buf) -> {
                buf.writeFloat(vec.getX());
                buf.writeFloat(vec.getY());
                buf.writeFloat(vec.getZ());
            },
            buf -> new Vec3f(buf.readFloat(), buf.readFloat(), buf.readFloat())
    );

    PacketCodec<Formatting> FORMATTING = PacketCodec.ofEnum(Formatting.class);

    static <T> PacketCodec<T> unit(Supplier<T> supplier) {
        return PacketCodec.of((value, buf) -> {}, buf -> supplier.get());
    }

    static <T extends Enum<T>> PacketCodec<@Nullable T> ofEnum(Class<T> enumClass) {
        var values = enumClass.getEnumConstants();
        return PacketCodec.of(
                (value, buf) -> buf.writeByte(value != null ? value.ordinal() & 0xFF : Byte.MIN_VALUE),
                buf -> {
                    int ordinal = buf.readUnsignedByte();
                    return ordinal >= 0 && ordinal < values.length ? values[ordinal] : null;
                }
        );
    }

    static <T> PacketCodec<@Nullable T> ofRegistry(Registry<T> registry) {
        return PacketCodec.of(
                (value, buf) -> buf.writeVarInt(registry.getRawId(value)),
                buf -> registry.get(buf.readVarInt())
        );
    }

    static <K, V> PacketCodec<Map<K, V>> mapOf(PacketCodec<K> keyCodec, PacketCodec<V> valueCodec) {
        return new MapOf<>(keyCodec, valueCodec);
    }

    static <T> PacketCodec<T> of(PacketEncoder<T> encoder, PacketDecoder<T> decoder) {
        return new PacketCodec<>() {
            @Override
            public void encode(T value, PacketByteBuf buf) {
                encoder.encode(value, buf);
            }

            @Override
            public T decode(PacketByteBuf buf) {
                return decoder.decode(buf);
            }
        };
    }

    @Override
    void encode(T value, PacketByteBuf buf);

    @Override
    T decode(PacketByteBuf buf);

    default PacketCodec<List<T>> list() {
        return new ListOf<>(this);
    }

    default PacketCodec<@Nullable T> nullable() {
        return new NullableOf<>(this);
    }

    default PacketCodec<T> orElse(T defaultValue) {
        return PacketCodec.of(
                (value, buf) -> this.encode(value != null ? value : defaultValue, buf),
                buf -> Objects.requireNonNullElse(this.decode(buf), defaultValue)
        );
    }

    default <R> PacketCodec<R> map(
            Function<T, R> mapTo,
            Function<R, T> mapFrom
    ) {
        return new PacketCodec<>() {
            @Override
            public void encode(R value, PacketByteBuf buf) {
                PacketCodec.this.encode(mapFrom.apply(value), buf);
            }

            @Override
            public R decode(PacketByteBuf buf) {
                return mapTo.apply(PacketCodec.this.decode(buf));
            }
        };
    }

    record MapOf<K, V>(PacketCodec<K> keyCodec, PacketCodec<V> valueCodec) implements PacketCodec<Map<K, V>> {
        @Override
        public void encode(Map<K, V> map, PacketByteBuf buf) {
            buf.writeVarInt(map.size());
            for (var entry : map.entrySet()) {
                this.keyCodec.encode(entry.getKey(), buf);
                this.valueCodec.encode(entry.getValue(), buf);
            }
        }

        @Override
        public Map<K, V> decode(PacketByteBuf buf) {
            int count = buf.readVarInt();
            var map = new Object2ObjectOpenHashMap<K, V>(count);
            for (int i = 0; i < count; i++) {
                var key = this.keyCodec.decode(buf);
                var value = this.valueCodec.decode(buf);
                map.put(key, value);
            }
            return map;
        }
    }

    record ListOf<T>(PacketCodec<T> elementCodec) implements PacketCodec<List<T>> {
        @Override
        public void encode(List<T> list, PacketByteBuf buf) {
            buf.writeVarInt(list.size());
            for (var element : list) {
                this.elementCodec.encode(element, buf);
            }
        }

        @Override
        public List<T> decode(PacketByteBuf buf) {
            int size = buf.readVarInt();
            List<T> list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                list.add(this.elementCodec.decode(buf));
            }
            return list;
        }
    }

    record NullableOf<T>(PacketCodec<T> valueCodec) implements PacketCodec<@Nullable T> {
        @Override
        public void encode(T value, PacketByteBuf buf) {
            buf.writeBoolean(value != null);
            if (value != null) {
                this.valueCodec.encode(value, buf);
            }
        }

        @Override
        @Nullable
        public T decode(PacketByteBuf buf) {
            return buf.readBoolean() ? this.valueCodec.decode(buf) : null;
        }
    }
}
