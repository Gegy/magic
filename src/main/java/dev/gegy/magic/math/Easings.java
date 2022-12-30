package dev.gegy.magic.math;

// Easing functions adapted from <https://easings.net/>.
public final class Easings {
    public static float easeInCirc(final float x) {
        return (float) (1.0f - Math.sqrt(1.0f - x * x));
    }

    public static float easeOutCirc(final float x) {
        return (float) Math.sqrt(2.0f * x - x * x);
    }
}
