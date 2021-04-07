package dev.gegy.magic.client.render;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.client.render.beam.BeamRenderData;
import dev.gegy.magic.client.render.beam.BeamRenderer;
import dev.gegy.magic.client.render.glyph.GlyphRenderData;
import dev.gegy.magic.client.render.glyph.GlyphRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.util.Collection;

public final class EffectRenderManager {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static EffectRenderManager instance;

    private GlyphRenderer glyphRenderer;
    private BeamRenderer beamRenderer;

    private final GlyphRenderData glyphRenderData = new GlyphRenderData();
    private final BeamRenderData beamRenderData = new BeamRenderData();

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
        ClientPlayerEntity player = CLIENT.player;
        if (player != null) {
            this.renderGlyphs(context);
            this.renderBeams(context);
        }
    }

    private void renderGlyphs(WorldRenderContext context) {
        GlyphRenderer glyphRenderer = this.glyphRenderer;
        if (glyphRenderer == null) {
            return;
        }

        ClientGlyphTracker glyphTracker = ClientGlyphTracker.INSTANCE;
        Collection<ClientGlyph> glyphs = glyphTracker.getGlyphs();
        ClientGlyph drawingGlyph = glyphTracker.getOwnDrawingGlyph();
        if (glyphs.isEmpty() && drawingGlyph == null) {
            return;
        }

        try (GlyphRenderer.Batcher batcher = glyphRenderer.start(CLIENT.getFramebuffer())) {
            for (ClientGlyph glyph : glyphs) {
                this.renderGlyph(batcher, glyph, context);
            }

            if (drawingGlyph != null) {
                this.renderGlyph(batcher, drawingGlyph, context);
            }
        }
    }

    private void renderGlyph(GlyphRenderer.Batcher renderer, ClientGlyph glyph, WorldRenderContext context) {
        GlyphRenderData renderData = this.glyphRenderData;
        renderData.set(glyph, context);
        renderer.render(renderData);
    }

    private void renderBeams(WorldRenderContext context) {
        BeamRenderer beamRenderer = this.beamRenderer;
        if (beamRenderer == null) {
            return;
        }

        // TODO: should not be identical to glyph rendering
        ClientGlyphTracker glyphTracker = ClientGlyphTracker.INSTANCE;
        Collection<ClientGlyph> glyphs = glyphTracker.getGlyphs();
        ClientGlyph drawingGlyph = glyphTracker.getOwnDrawingGlyph();
        if (glyphs.isEmpty() && drawingGlyph == null) {
            return;
        }

        try (BeamRenderer.Batcher batcher = beamRenderer.start(CLIENT.getFramebuffer())) {
            for (ClientGlyph glyph : glyphs) {
                this.renderBeam(batcher, glyph, context);
            }

            if (drawingGlyph != null) {
                this.renderBeam(batcher, drawingGlyph, context);
            }
        }
    }

    private void renderBeam(BeamRenderer.Batcher renderer, ClientGlyph glyph, WorldRenderContext context) {
        BeamRenderData renderData = this.beamRenderData;
        renderData.set(glyph, context);
        renderer.render(renderData);
    }

    private void load(ResourceManager resources) {
        this.close();

        try {
            this.glyphRenderer = GlyphRenderer.create(resources);
        } catch (IOException e) {
            Magic.LOGGER.error("Failed to create glyph renderer", e);
        }

        try {
            this.beamRenderer = BeamRenderer.create(resources);
        } catch (IOException e) {
            Magic.LOGGER.error("Failed to create beam renderer", e);
        }
    }

    private void close() {
        if (this.glyphRenderer != null) {
            this.glyphRenderer.close();
            this.glyphRenderer = null;
        }

        if (this.beamRenderer != null) {
            this.beamRenderer.close();
            this.beamRenderer = null;
        }
    }
}
