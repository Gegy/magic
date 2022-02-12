package dev.gegy.magic.client.render.gl;

public interface GlObject extends AutoCloseable {
    void delete();

    @Override
    default void close() {
        this.delete();
    }
}
