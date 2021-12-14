package dev.gegy.magic.client.effect;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.effect.beam.BeamEffects;
import dev.gegy.magic.client.effect.beam.BeamRenderParameters;
import dev.gegy.magic.client.effect.glyph.GlyphEffects;
import dev.gegy.magic.client.effect.glyph.GlyphRenderParameters;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.spellcasting.ClientSpellcastingTracker;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Collection;

public final class EffectRenderManager {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static EffectRenderManager instance;

    private GlyphEffects glyphEffects;
    private BeamEffects beamEffects;

    private final GlyphRenderParameters glyphParameters = new GlyphRenderParameters();
    private final BeamRenderParameters beamParameters = new BeamRenderParameters();

    public static void onInitialize() {
        if (EffectRenderManager.instance != null) {
            throw new IllegalStateException("effect render manager already initialized");
        }

        EffectRenderManager instance = new EffectRenderManager();
        EffectRenderManager.instance = instance;

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Magic.identifier("effect_renderer");
            }

            @Override
            public void reload(ResourceManager resources) {
                instance.load(resources);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(instance::tick);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(instance::render);
    }

    public static EffectRenderManager get() {
        EffectRenderManager instance = EffectRenderManager.instance;
        if (instance == null) {
            throw new IllegalStateException("effect render manager not yet initialized");
        }
        return instance;
    }

    private void render(WorldRenderContext context) {
        if (CLIENT.world == null) return;

        GlyphEffects glyphEffects = this.glyphEffects;
        if (glyphEffects != null) {
            this.renderGlyphs(context, glyphEffects);
        }

        BeamEffects beamEffects = this.beamEffects;
        if (beamEffects != null) {
            this.renderBeams(context, beamEffects);
        }
    }

    private void tick(MinecraftClient client) {
        ClientWorld world = CLIENT.world;
        if (world == null) return;

        ParticleManager particleManager = client.particleManager;

        // TODO: generic spell effect system
        BeamEffects beamEffects = this.beamEffects;
        if (beamEffects != null) {
            for (ClientGlyph glyph : ClientSpellcastingTracker.INSTANCE.getGlyphs()) {
                beamEffects.spawnParticles(particleManager, glyph);
            }
        }
    }

    private void renderGlyphs(WorldRenderContext context, GlyphEffects glyphEffects) {
        Collection<ClientGlyph> glyphs = ClientSpellcastingTracker.INSTANCE.getGlyphs();
        if (glyphs.isEmpty()) {
            return;
        }

        try (GlyphEffects.Renderer renderer = glyphEffects.renderTo(CLIENT.getFramebuffer())) {
            GlyphRenderParameters parameters = this.glyphParameters;
            for (ClientGlyph glyph : glyphs) {
                parameters.set(glyph, context);
                renderer.render(parameters);
            }
        }
    }

    private void renderBeams(WorldRenderContext context, BeamEffects beamEffects) {
        Collection<ClientGlyph> glyphs = ClientSpellcastingTracker.INSTANCE.getGlyphs();
        if (glyphs.isEmpty()) {
            return;
        }

        try (BeamEffects.Renderer renderer = beamEffects.renderTo(CLIENT.getFramebuffer())) {
            BeamRenderParameters parameters = this.beamParameters;
            for (ClientGlyph glyph : glyphs) {
                parameters.set(glyph, context);
                renderer.render(parameters);
            }
        }
    }

    private void load(ResourceManager resources) {
        this.close();

        try {
            this.glyphEffects = GlyphEffects.create(resources);
        } catch (IOException e) {
            Magic.LOGGER.error("Failed to create glyph effects", e);
        }

        try {
            this.beamEffects = BeamEffects.create(resources);
        } catch (IOException e) {
            Magic.LOGGER.error("Failed to create beam effects", e);
        }
    }

    private void close() {
        if (this.glyphEffects != null) {
            this.glyphEffects.close();
            this.glyphEffects = null;
        }

        if (this.beamEffects != null) {
            this.beamEffects.close();
            this.beamEffects = null;
        }
    }
}
