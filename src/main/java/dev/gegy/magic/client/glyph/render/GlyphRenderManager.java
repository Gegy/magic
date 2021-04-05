package dev.gegy.magic.client.glyph.render;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.client.glyph.GlyphColor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.Collection;

public final class GlyphRenderManager {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static GlyphRenderManager instance;

    private GlyphRenderer glyphRenderer;

    private final GlyphRenderData renderData = new GlyphRenderData();

    public static void onInitialize() {
        if (GlyphRenderManager.instance != null) {
            throw new IllegalStateException("glyph render manager already initialized");
        }

        GlyphRenderManager instance = new GlyphRenderManager();
        GlyphRenderManager.instance = instance;

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Magic.identifier("glyph_renderer");
            }

            @Override
            public void reload(ResourceManager resources) {
                instance.load(resources);
            }
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(instance::render);
    }

    public static GlyphRenderManager get() {
        GlyphRenderManager instance = GlyphRenderManager.instance;
        if (instance == null) {
            throw new IllegalStateException("glyph render manager not yet initialized");
        }
        return instance;
    }

    public void render(WorldRenderContext context) {
        ClientPlayerEntity player = CLIENT.player;
        GlyphRenderer glyphRenderer = this.glyphRenderer;
        if (glyphRenderer == null || player == null) {
            return;
        }

        ClientGlyphTracker glyphTracker = ClientGlyphTracker.INSTANCE;
        Collection<ClientGlyph> glyphs = glyphTracker.getGlyphs();
        ClientGlyph drawingGlyph = glyphTracker.getOwnDrawingGlyph();
        if (glyphs.isEmpty() && drawingGlyph == null) {
            return;
        }

        Matrix4f projectionMatrix = context.projectionMatrix();
        try (GlyphRenderer.Batcher batcher = glyphRenderer.start(CLIENT.getFramebuffer(), projectionMatrix)) {
            for (ClientGlyph glyph : glyphs) {
                this.renderGlyph(batcher, glyph, player, context);
            }

            if (drawingGlyph != null) {
                this.renderGlyph(batcher, drawingGlyph, player, context);
            }
        }
    }

    private void renderGlyph(GlyphRenderer.Batcher renderer, ClientGlyph glyph, ClientPlayerEntity player, WorldRenderContext context) {
        Matrix4f modelMatrix = context.matrixStack().peek().getModel();
        Vec3d cameraPos = context.camera().getPos();
        float tickDelta = context.tickDelta();

        Entity source = glyph.source;
        Vec3d sourcePos = source.getCameraPosVec(tickDelta);

        GlyphRenderData renderData = this.renderData;

        renderData.glyphToWorld.method_35434(modelMatrix);

        float translationX = (float) (sourcePos.x - cameraPos.x);
        float translationY = (float) (sourcePos.y - cameraPos.y);
        float translationZ = (float) (sourcePos.z - cameraPos.z);
        renderData.glyphToWorld.multiplyByTranslation(translationX, translationY, translationZ);

        renderData.glyphToWorld.multiply(glyph.transform.getTransformationMatrix(tickDelta));

        renderData.radius = glyph.radius;
        renderData.formProgress = glyph.getFormProgress(player.world.getTime(), tickDelta);

        GlyphColor primaryColor = glyph.getPrimaryColor();
        renderData.primaryRed = primaryColor.getRed(tickDelta);
        renderData.primaryGreen = primaryColor.getGreen(tickDelta);
        renderData.primaryBlue = primaryColor.getBlue(tickDelta);

        GlyphColor secondaryColor = glyph.getSecondaryColor();
        renderData.secondaryRed = secondaryColor.getRed(tickDelta);
        renderData.secondaryGreen = secondaryColor.getGreen(tickDelta);
        renderData.secondaryBlue = secondaryColor.getBlue(tickDelta);

        renderData.shape = glyph.shape;
        renderData.highlightNodes = glyph.source == player;
        renderData.stroke = glyph.stroke;

        renderer.render(renderData, tickDelta);
    }

    private void load(ResourceManager resources) {
        this.close();

        try {
            this.glyphRenderer = GlyphRenderer.create(resources);
        } catch (IOException e) {
            Magic.LOGGER.error("Failed to create glyph renderer", e);
        }
    }

    private void close() {
        if (this.glyphRenderer != null) {
            this.glyphRenderer.close();
            this.glyphRenderer = null;
        }
    }
}
