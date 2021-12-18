package dev.gegy.magic.math;

public final record ColorRgb(float red, float green, float blue) {
    public ColorRgb(int packed) {
        this(
                (packed >> 16 & 0xFF) / 255.0F,
                (packed >> 8 & 0xFF) / 255.0F,
                (packed & 0xFF) / 255.0F
        );
    }
}
