package dev.gegy.magic.math;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.util.Mth;
import org.hsluv.HUSLColorConverter;

public final class ColorRgb {
    public static final PacketCodec<ColorRgb> PACKET_CODEC = PacketCodec.of(
            (color, buf) -> buf.writeInt(color.packed()),
            buf -> ColorRgb.of(buf.readInt())
    );

    public static final ColorRgb WHITE = ColorRgb.of(1.0f, 1.0f, 1.0f);

    private final float red;
    private final float green;
    private final float blue;
    private final int packed;

    private ColorRgb(final float red, final float green, final float blue, final int packed) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.packed = packed;
    }

    public static ColorRgb of(final float red, final float green, final float blue) {
        final int redValue = Mth.floor(red * 255.0f) & 0xFF;
        final int greenValue = Mth.floor(green * 255.0f) & 0xFF;
        final int blueValue = Mth.floor(blue * 255.0f) & 0xFF;
        final int packed = redValue << 16 | greenValue << 8 | blueValue;

        return new ColorRgb(red, green, blue, packed);
    }

    public static ColorRgb of(final int packed) {
        final float red = (packed >> 16 & 0xFF) / 255.0f;
        final float green = (packed >> 8 & 0xFF) / 255.0f;
        final float blue = (packed & 0xFF) / 255.0f;
        return new ColorRgb(red, green, blue, packed);
    }

    static ColorRgb fromTuple(final double[] tuple) {
        return ColorRgb.of((float) tuple[0], (float) tuple[1], (float) tuple[2]);
    }

    public float red() {
        return red;
    }

    public float green() {
        return green;
    }

    public float blue() {
        return blue;
    }

    public int packed() {
        return packed;
    }

    public ColorHsluv toHsluv() {
        return ColorHsluv.fromTuple(HUSLColorConverter.rgbToHsluv(toTuple()));
    }

    double[] toTuple() {
        return new double[] { red, green, blue };
    }

    @Override
    public String toString() {
        return Integer.toHexString(packed);
    }
}
