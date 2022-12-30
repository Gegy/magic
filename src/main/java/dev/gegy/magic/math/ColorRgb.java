package dev.gegy.magic.math;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.util.Mth;
import org.hsluv.HUSLColorConverter;

public final class ColorRgb {
    public static final PacketCodec<ColorRgb> PACKET_CODEC = PacketCodec.of(
            (color, buf) -> buf.writeInt(color.packed()),
            buf -> ColorRgb.of(buf.readInt())
    );

    public static final ColorRgb WHITE = ColorRgb.of(1.0F, 1.0F, 1.0F);

    private final float red;
    private final float green;
    private final float blue;
    private final int packed;

    private ColorRgb(float red, float green, float blue, int packed) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.packed = packed;
    }

    public static ColorRgb of(float red, float green, float blue) {
        int redValue = Mth.floor(red * 255.0F) & 0xFF;
        int greenValue = Mth.floor(green * 255.0F) & 0xFF;
        int blueValue = Mth.floor(blue * 255.0F) & 0xFF;
        int packed = redValue << 16 | greenValue << 8 | blueValue;

        return new ColorRgb(red, green, blue, packed);
    }

    public static ColorRgb of(int packed) {
        float red = (packed >> 16 & 0xFF) / 255.0F;
        float green = (packed >> 8 & 0xFF) / 255.0F;
        float blue = (packed & 0xFF) / 255.0F;
        return new ColorRgb(red, green, blue, packed);
    }

    static ColorRgb fromTuple(double[] tuple) {
        return ColorRgb.of((float) tuple[0], (float) tuple[1], (float) tuple[2]);
    }

    public float red() {
        return this.red;
    }

    public float green() {
        return this.green;
    }

    public float blue() {
        return this.blue;
    }

    public int packed() {
        return this.packed;
    }

    public ColorHsluv toHsluv() {
        return ColorHsluv.fromTuple(HUSLColorConverter.rgbToHsluv(this.toTuple()));
    }

    double[] toTuple() {
        return new double[] { this.red, this.green, this.blue };
    }

    @Override
    public String toString() {
        return Integer.toHexString(this.packed);
    }
}
