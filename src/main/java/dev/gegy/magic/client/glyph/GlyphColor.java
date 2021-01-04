package dev.gegy.magic.client.glyph;

import net.minecraft.util.math.MathHelper;

import java.awt.Color;

public final class GlyphColor {
    // TODO: should these come from a default "null" spell?
    static final float[] DEFAULT_PRIMARY = new float[] { 1.0F, 0.9F, 0.2F };
    static final float[] DEFAULT_SECONDARY = primaryToSecondary(DEFAULT_PRIMARY[0], DEFAULT_PRIMARY[1], DEFAULT_PRIMARY[2]);

    private float red, green, blue;
    private float targetRed, targetGreen, targetBlue;
    private float prevRed, prevGreen, prevBlue;

    public GlyphColor(float red, float green, float blue) {
        this.red = this.targetRed = this.prevRed = red;
        this.green = this.targetGreen = this.prevGreen = green;
        this.blue = this.targetBlue = this.prevBlue = blue;
    }

    public GlyphColor(float[] color) {
        this(color[0], color[1], color[2]);
    }

    public void tick(float lerpSpeed) {
        this.prevRed = this.red;
        this.prevGreen = this.green;
        this.prevBlue = this.blue;

        this.red += (this.targetRed - this.red) * lerpSpeed;
        this.green += (this.targetGreen - this.green) * lerpSpeed;
        this.blue += (this.targetBlue - this.blue) * lerpSpeed;
    }

    public void set(float red, float green, float blue) {
        this.targetRed = red;
        this.targetGreen = green;
        this.targetBlue = blue;
    }

    public void set(float[] color) {
        this.set(color[0], color[1], color[2]);
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

    public static float[] primaryToSecondary(float red, float green, float blue) {
        float[] buffer = new float[3];

        Color.RGBtoHSB(
                MathHelper.floor(red * 255.0F),
                MathHelper.floor(green * 255.0F),
                MathHelper.floor(blue * 255.0F),
                buffer
        );
        float hue = buffer[0];
        float saturation = buffer[1];
        float brightness = buffer[2];

        saturation *= 0.3F;
        hue = warmHue(hue, 0.05F);

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        buffer[0] = (rgb >> 16 & 0xFF) / 255.0F;
        buffer[1] = (rgb >> 8 & 0xFF) / 255.0F;
        buffer[2] = (rgb & 0xFF) / 255.0F;

        return buffer;
    }

    private static float warmHue(float hue, float amount) {
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
