package dev.gegy.magic.client.render.gl;

public interface GlBinding extends AutoCloseable {
    void unbind();

    @Override
    default void close() {
        this.unbind();
    }
}
