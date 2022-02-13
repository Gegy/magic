package dev.gegy.magic.client.effect.casting.spell.teleport;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class TeleportEffectSystem implements EffectSystem {
    private final TeleportEffectRenderer renderer = new TeleportEffectRenderer();

    public static TeleportEffectSystem create(ResourceManager resources) throws IOException {
        return new TeleportEffectSystem();
    }

    @Override
    public void render(MinecraftClient client, WorldRenderContext context, Framebuffer targetFramebuffer, EffectSelector effects) {
        RenderSystem.disableCull();

        for (var effect : effects.select(TeleportEffect.TYPE)) {
            this.renderer.render(client, context, effect);
        }

        RenderSystem.enableCull();
    }

    @Override
    public void tick(MinecraftClient client, EffectSelector effects) {
    }

    @Override
    public void close() {
    }
}
