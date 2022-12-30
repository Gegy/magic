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

    static <T> PacketCodec<@Nullable T> ofRegistry(IdMap<T> registry) {
        return PacketCodec.of(
                (value, buf) -> buf.writeId(registry, value),
                buf -> buf.readById(registry)
        );
    }

    static <K, V> PacketCodec<Map<K, V>> mapOf(PacketCodec<K> keyCodec, PacketCodec<V> valueCodec) {
        return new MapOf<>(keyCodec, valueCodec);
    }

    static <T> PacketCodec<T> of(PacketEncoder<T> encoder, PacketDecoder<T> decoder) {
        return new PacketCodec<>() {
            @Override
            public void encode(T value, FriendlyByteBuf buf) {
                encoder.encode(value, buf);
            }

            @Override
            public T decode(FriendlyByteBuf buf) {
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
            public void encode(R value, FriendlyByteBuf buf) {
                PacketCodec.this.encode(mapFrom.apply(value), buf);
            }

            @Override
            public R decode(FriendlyByteBuf buf) {
                return mapTo.apply(PacketCodec.this.decode(buf));
            }
        };
    }

    record MapOf<K, V>(PacketCodec<K> keyCodec, PacketCodec<V> valueCodec) implements PacketCodec<Map<K, V>> {
        @Override
        public void encode(Map<K, V> map, FriendlyByteBuf buf) {
            buf.writeVarInt(map.size());
            for (var entry : map.entrySet()) {
                this.keyCodec.encode(entry.getKey(), buf);
                this.valueCodec.encode(entry.getValue(), buf);
            }
        }

        @Override
        public Map<K, V> decode(FriendlyByteBuf buf) {
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
        public void encode(List<T> list, FriendlyByteBuf buf) {
            buf.writeVarInt(list.size());
            for (var element : list) {
                this.elementCodec.encode(element, buf);
            }
        }

        @Override
        public List<T> decode(FriendlyByteBuf buf) {
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
        public void encode(T value, FriendlyByteBuf buf) {
            buf.writeBoolean(value != null);
            if (value != null) {
                this.valueCodec.encode(value, buf);
            }
        }

        @Override
        @Nullable
        public T decode(FriendlyByteBuf buf) {
            return buf.readBoolean() ? this.valueCodec.decode(buf) : null;
        }
    }
}
