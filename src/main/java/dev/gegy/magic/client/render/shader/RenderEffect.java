package dev.gegy.magic.client.render.shader;

public interface RenderEffect<T> extends AutoCloseable {
    void bind(T data);

    void unbind();

    @Override
    void close();
}
