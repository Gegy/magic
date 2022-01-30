package dev.gegy.magic.math;

public final class AnimationTimer {
    private final int length;
    private int time;

    public AnimationTimer(int length) {
        this.length = length;
    }

    public boolean tick() {
        return this.time++ > this.length;
    }

    public float getProgress(float tickDelta) {
        float time = Math.min(this.time + tickDelta, this.length);
        return time / this.length;
    }
}
