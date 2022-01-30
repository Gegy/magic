package dev.gegy.magic.client.effect;

public final class EffectType<E extends Effect> {
    private EffectType() {
    }

    public static <E extends Effect> EffectType<E> create() {
        return new EffectType<>();
    }
}
