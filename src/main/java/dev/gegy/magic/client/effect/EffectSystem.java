package dev.gegy.magic.client.effect;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public interface EffectSystem extends AutoCloseable {
    void render(MinecraftClient client, WorldRenderContext context, Framebuffer targetFramebuffer, EffectSelector effects);

    default void tick(MinecraftClient client, EffectSelector effects) {
    }

    @Override
    void close();

    interface Factory<E extends EffectSystem> {
        E create(ResourceManager resources) throws IOException;
    }
}
