package dev.gegy.magic.math;

import net.minecraft.util.Mth;

public final class AnimatedColor {
    private float red, green, blue;
    private float prevRed, prevGreen, prevBlue;
    private float targetRed, targetGreen, targetBlue;

    public AnimatedColor(final ColorRgb color) {
        red = targetRed = prevRed = color.red();
        green = targetGreen = prevGreen = color.green();
        blue = targetBlue = prevBlue = color.blue();
    }

    public void tick(final float lerpSpeed) {
        prevRed = red;
        prevGreen = green;
        prevBlue = blue;

        red += (targetRed - red) * lerpSpeed;
        green += (targetGreen - green) * lerpSpeed;
        blue += (targetBlue - blue) * lerpSpeed;
    }

    public void set(final ColorRgb color) {
        targetRed = color.red();
        targetGreen = color.green();
        targetBlue = color.blue();
    }

    public float getRed(final float tickDelta) {
        return Mth.lerp(tickDelta, prevRed, red);
    }

    public float getGreen(final float tickDelta) {
        return Mth.lerp(tickDelta, prevGreen, green);
    }

    public float getBlue(final float tickDelta) {
        return Mth.lerp(tickDelta, prevBlue, blue);
    }

    public ColorRgb get(final float tickDelta) {
        return ColorRgb.of(getRed(tickDelta), getGreen(tickDelta), getBlue(tickDelta));
    }

    public ColorRgb target() {
        return ColorRgb.of(targetRed, targetGreen, targetBlue);
    }
}
