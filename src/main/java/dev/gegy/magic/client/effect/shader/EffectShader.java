package dev.gegy.magic.client.effect.shader;

import dev.gegy.magic.client.render.gl.GlBinding;

public interface EffectShader<T> extends AutoCloseable {
    GlBinding bind(T parameters);

    void delete();

    @Override
    default void close() {
        delete();
    }
}
