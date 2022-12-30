package dev.gegy.magic.client.effect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public interface EffectSystem extends AutoCloseable {
    void render(Minecraft client, WorldRenderContext context, RenderTarget targetFramebuffer, EffectSelector effects);

    default void tick(Minecraft client, EffectSelector effects) {
    }

    @Override
    void close();

    interface Factory<E extends EffectSystem> {
        E create(ResourceManager resources) throws IOException;
    }
}
