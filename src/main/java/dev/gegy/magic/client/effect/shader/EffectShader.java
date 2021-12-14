package dev.gegy.magic.client.effect.shader;

public interface EffectShader<T> extends AutoCloseable {
    void bind(T parameters);

    void unbind();

    @Override
    void close();
}
