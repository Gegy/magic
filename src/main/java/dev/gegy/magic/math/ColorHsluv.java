package dev.gegy.magic.math;

import net.minecraft.util.Mth;
import org.hsluv.HUSLColorConverter;

public record ColorHsluv(float hue, float saturation, float light) {
    public static ColorHsluv of(final float hue, final float saturation, final float light) {
        return new ColorHsluv(hue, saturation, light);
    }

    static ColorHsluv fromTuple(final double[] tuple) {
        return new ColorHsluv((float) (tuple[0] / 360.0), (float) (tuple[1] / 100.0), (float) (tuple[2] / 100.0));
    }

    public ColorHsluv warmHue(final float amount) {
        return withHue(warmHue(hue, amount));
    }

    public ColorHsluv coolHue(final float amount) {
        return warmHue(-amount);
    }

    public ColorHsluv mulSaturation(final float factor) {
        final float saturation = Mth.clamp(this.saturation * factor, 0.0f, 1.0f);
        return withSaturation(saturation);
    }

    public ColorHsluv mulLight(final float factor) {
        final float light = Mth.clamp(this.light * factor, 0.0f, 1.0f);
        return withLight(light);
    }

    public ColorHsluv withHue(final float hue) {
        return new ColorHsluv(hue, saturation, light);
    }

    public ColorHsluv withSaturation(final float saturation) {
        return new ColorHsluv(hue, saturation, light);
    }

    public ColorHsluv withLight(final float light) {
        return new ColorHsluv(hue, saturation, light);
    }

    public ColorRgb toRgb() {
        return ColorRgb.fromTuple(HUSLColorConverter.hsluvToRgb(toTuple()));
    }

    double[] toTuple() {
        return new double[] { hue * 360.0, saturation * 100.0, light * 100.0 };
    }

    public static float warmHue(float hue, final float amount) {
        if (hue < amount || hue > 0.65f) {
            hue += amount;
        } else {
            hue -= amount;
        }

        if (hue > 1.0f) hue -= 1.0f;
        if (hue < 0.0f) hue += 1.0f;

        return hue;
    }
}
