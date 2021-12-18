package dev.gegy.magic.client.glyph;

import dev.gegy.magic.math.ColorRgb;
import net.minecraft.util.math.MathHelper;

public final class FadingColor {
    private float red, green, blue;
    private float targetRed, targetGreen, targetBlue;
    private float prevRed, prevGreen, prevBlue;

    public FadingColor(ColorRgb color) {
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
        return MathHelper.lerp(tickDelta, this.prevRed, this.red);
    }

    public float getGreen(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevGreen, this.green);
    }

    public float getBlue(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevBlue, this.blue);
    }
}
