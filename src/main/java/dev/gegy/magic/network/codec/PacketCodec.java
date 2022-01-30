package dev.gegy.magic.network.codec;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PacketCodec<T> extends PacketEncoder<T>, PacketDecoder<T> {
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

    static <T> PacketCodec<T> unit(Supplier<T> supplier) {
        return PacketCodec.of((value, buf) -> {}, buf -> supplier.get());
    }

    static <T> PacketCodec<@Nullable T> ofRegistry(Registry<T> registry) {
        return PacketCodec.of(
                (value, buf) -> buf.writeVarInt(registry.getRawId(value)),
                buf -> registry.get(buf.readVarInt())
        );
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
