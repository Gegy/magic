package dev.gegy.magic.math;

import net.minecraft.util.Mth;

public final class AnimatedColor {
    private float red, green, blue;
    private float prevRed, prevGreen, prevBlue;
    private float targetRed, targetGreen, targetBlue;

    public AnimatedColor(ColorRgb color) {
        this.red = this.targetRed = this.prevRed = color.red();
        this.green = this.targetGreen = this.prevGreen = color.green();
        this.blue = this.targetBlue = this.prevBlue = color.blue();
    }

    public void tick(float lerpSpeed) {
        this.prevRed = this.red;
        this.prevGreen = this.green;
        this.prevBlue = this.blue;

        this.red += (this.targetRed - this.red) * lerpSpeed;
        this.green += (this.targetGreen - this.green) * lerpSpeed;
        this.blue += (this.targetBlue - this.blue) * lerpSpeed;
    }

    public void set(ColorRgb color) {
        this.targetRed = color.red();
        this.targetGreen = color.green();
        this.targetBlue = color.blue();
    }

    public float getRed(float tickDelta) {
        return Mth.lerp(tickDelta, this.prevRed, this.red);
    }

    public float getGreen(float tickDelta) {
        return Mth.lerp(tickDelta, this.prevGreen, this.green);
    }

    public float getBlue(float tickDelta) {
        return Mth.lerp(tickDelta, this.prevBlue, this.blue);
    }

    public ColorRgb get(float tickDelta) {
        return ColorRgb.of(this.getRed(tickDelta), this.getGreen(tickDelta), this.getBlue(tickDelta));
    }

    public ColorRgb target() {
        return ColorRgb.of(this.targetRed, this.targetGreen, this.targetBlue);
    }
}
