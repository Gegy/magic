package dev.gegy.magic.client.effect.casting.spell.teleport;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public final class TeleportEffectSystem implements EffectSystem {
    private final TeleportEffectRenderer renderer = new TeleportEffectRenderer();

    public static TeleportEffectSystem create(final ResourceManager resources) throws IOException {
        return new TeleportEffectSystem();
    }

    @Override
    public void render(final Minecraft client, final WorldRenderContext context, final RenderTarget targetFramebuffer, final EffectSelector effects) {
        RenderSystem.disableCull();

        for (final TeleportEffect effect : effects.select(TeleportEffect.TYPE)) {
            renderer.render(client, context, effect);
        }

        RenderSystem.enableCull();
    }

    @Override
    public void tick(final Minecraft client, final EffectSelector effects) {
    }

    @Override
    public void close() {
    }
}
