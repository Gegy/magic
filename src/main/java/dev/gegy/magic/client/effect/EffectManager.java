package dev.gegy.magic.client.effect;

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

        EffectManager instance = new EffectManager();
        EffectManager.instance = instance;

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public ResourceLocation getFabricId() {
                return Magic.identifier("effects");
            }

            @Override
            public void onResourceManagerReload(ResourceManager resources) {
                instance.load(resources);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(instance::tick);
        WorldRenderEvents.AFTER_ENTITIES.register(instance::render);
    }

    public static EffectManager get() {
        EffectManager instance = EffectManager.instance;
        if (instance == null) {
            throw new IllegalStateException("effect manager not yet initialized");
        }
        return instance;
    }

    public void add(Effect effect) {
        this.effects.add(effect);
    }

    public boolean remove(Effect effect) {
        return this.effects.remove(effect);
    }

    private void render(WorldRenderContext context) {
        if (context.world() != null) {
            // TODO: handle fabulous framebuffer
            var framebuffer = CLIENT.getMainRenderTarget();
            for (var system : this.effectSystems) {
                system.render(CLIENT, context, framebuffer, this.effects.selector());
            }
        }
    }

    private void tick(Minecraft client) {
        if (client.level != null) {
            for (var system : this.effectSystems) {
                system.tick(client, this.effects.selector());
            }
        }
    }

    private void load(ResourceManager resources) {
        this.close();

        this.tryLoadSystem(resources, GlyphEffectSystem::create);
        this.tryLoadSystem(resources, BeamEffectSystem::create);
        this.tryLoadSystem(resources, TeleportEffectSystem::create);
    }

    private void tryLoadSystem(ResourceManager resources, EffectSystem.Factory<?> factory) {
        try {
            var system = factory.create(resources);
            this.effectSystems.add(system);
        } catch (IOException e) {
            Magic.LOGGER.error("Failed to create effect system with {}", factory, e);
        }
    }

    private void close() {
        for (var system : this.effectSystems) {
            system.close();
        }
        this.effectSystems.clear();
    }
}
