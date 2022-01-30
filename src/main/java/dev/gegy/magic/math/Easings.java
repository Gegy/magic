package dev.gegy.magic.math;

// Easing functions adapted from <https://easings.net/>.
public final class Easings {
    public static float easeInCirc(float x) {
        return (float) (1.0F - Math.sqrt(1.0F - x * x));
    }

    public static float easeOutCirc(float x) {
        return (float) Math.sqrt(2.0F * x - x * x);
    }
}
