package dev.gegy.magic.client.effect;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.casting.spell.beam.BeamEffectSystem;
import dev.gegy.magic.client.effect.casting.spell.teleport.TeleportEffectSystem;
import dev.gegy.magic.client.effect.glyph.GlyphEffectSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class EffectManager {
    private static final Minecraft CLIENT = Minecraft.getInstance();

    private static EffectManager instance;

    private final GlobalEffectList effects = new GlobalEffectList();

    private final List<EffectSystem> effectSystems = new ArrayList<>();

    public static void onInitialize() {
        if (EffectManager.instance != null) {
            throw new IllegalStateException("effect render manager already initialized");
        }

        final EffectManager instance = new EffectManager();
        EffectManager.instance = instance;

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Magic.identifier("effects");
            }

            @Override
            public void onResourceManagerReload(final ResourceManager resources) {
                instance.load(resources);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(instance::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(instance::render);
    }

    public static EffectManager get() {
        final EffectManager instance = EffectManager.instance;
        if (instance == null) {
            throw new IllegalStateException("effect manager not yet initialized");
        }
        return instance;
    }

    public void add(final Effect effect) {
        effects.add(effect);
    }

    public boolean remove(final Effect effect) {
        return effects.remove(effect);
    }

    private void render(final WorldRenderContext context) {
        if (context.world() != null) {
            // TODO: handle fabulous framebuffer
            final RenderTarget framebuffer = CLIENT.getMainRenderTarget();
            for (final EffectSystem system : effectSystems) {
                system.render(CLIENT, context, framebuffer, effects.selector());
            }
        }
    }

    private void tick(final Minecraft client) {
        if (client.level != null) {
            for (final EffectSystem system : effectSystems) {
                system.tick(client, effects.selector());
            }
        }
    }

    private void load(final ResourceManager resources) {
        close();

        tryLoadSystem(resources, GlyphEffectSystem::create);
        tryLoadSystem(resources, BeamEffectSystem::create);
        tryLoadSystem(resources, TeleportEffectSystem::create);
    }

    private void tryLoadSystem(final ResourceManager resources, final EffectSystem.Factory<?> factory) {
        try {
            final EffectSystem system = factory.create(resources);
            effectSystems.add(system);
        } catch (final IOException e) {
            Magic.LOGGER.error("Failed to create effect system with {}", factory, e);
        }
    }

    private void close() {
        for (final EffectSystem system : effectSystems) {
            system.close();
        }
        effectSystems.clear();
    }
}
