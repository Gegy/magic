package dev.gegy.magic.math;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.MathHelper;

public final record ColorRgb(float red, float green, float blue) {
    public static final PacketCodec<ColorRgb> PACKET_CODEC = PacketCodec.of(ColorRgb::encode, ColorRgb::decode);

    public static final ColorRgb WHITE = new ColorRgb(1.0F, 1.0F, 1.0F);

    public ColorRgb(int packed) {
        this(
                (packed >> 16 & 0xFF) / 255.0F,
                (packed >> 8 & 0xFF) / 255.0F,
                (packed & 0xFF) / 255.0F
        );
    }

    public int packed() {
        int red = MathHelper.floor(this.red * 255.0F) & 0xFF;
        int green = MathHelper.floor(this.green * 255.0F) & 0xFF;
        int blue = MathHelper.floor(this.blue * 255.0F) & 0xFF;
        return red << 16 | green << 8 | blue;
    }

    private void encode(PacketByteBuf buf) {
        buf.writeInt(this.packed());
    }

    private static ColorRgb decode(PacketByteBuf buf) {
        return new ColorRgb(buf.readInt());
    }
}
