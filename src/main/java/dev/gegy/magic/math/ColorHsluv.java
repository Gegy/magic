package dev.gegy.magic.math;

import net.minecraft.util.math.MathHelper;
import org.hsluv.HUSLColorConverter;

public final record ColorHsluv(float hue, float saturation, float light) {
    public static ColorHsluv of(float hue, float saturation, float light) {
        return new ColorHsluv(hue, saturation, light);
    }

    static ColorHsluv fromTuple(double[] tuple) {
        return new ColorHsluv((float) (tuple[0] / 360.0), (float) (tuple[1] / 100.0), (float) (tuple[2] / 100.0));
    }

    public ColorHsluv warmHue(float amount) {
        return this.withHue(warmHue(this.hue, amount));
    }

    public ColorHsluv coolHue(float amount) {
        return this.warmHue(-amount);
    }

    public ColorHsluv mulSaturation(float factor) {
        float saturation = MathHelper.clamp(this.saturation * factor, 0.0F, 1.0F);
        return this.withSaturation(saturation);
    }

    public ColorHsluv mulLight(float factor) {
        float light = MathHelper.clamp(this.light * factor, 0.0F, 1.0F);
        return this.withLight(light);
    }

    public ColorHsluv withHue(float hue) {
        return new ColorHsluv(hue, this.saturation, this.light);
    }

    public ColorHsluv withSaturation(float saturation) {
        return new ColorHsluv(this.hue, saturation, this.light);
    }

    public ColorHsluv withLight(float light) {
        return new ColorHsluv(this.hue, this.saturation, light);
    }

    public ColorRgb toRgb() {
        return ColorRgb.fromTuple(HUSLColorConverter.hsluvToRgb(this.toTuple()));
    }

    double[] toTuple() {
        return new double[] { this.hue * 360.0, this.saturation * 100.0, this.light * 100.0 };
    }

    public static float warmHue(float hue, float amount) {
        if (hue < amount || hue > 0.65F) {
            hue += amount;
        } else {
            hue -= amount;
        }

        if (hue > 1.0F) hue -= 1.0F;
        if (hue < 0.0F) hue += 1.0F;

        return hue;
    }
}
