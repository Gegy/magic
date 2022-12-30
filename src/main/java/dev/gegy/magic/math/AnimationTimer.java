package dev.gegy.magic.math;

public final class AnimationTimer {
    private final int length;
    private int time;

    public AnimationTimer(final int length) {
        this.length = length;
    }

    public boolean tick() {
        return time++ > length;
    }

    public float getProgress(final float tickDelta) {
        final float time = Math.min(this.time + tickDelta, length);
        return time / length;
    }
}
