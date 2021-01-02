package dev.gegy.magic.client.glyph.render;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.math.Matrix4fAccess;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.Collection;

public final class GlyphRenderManager {
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
            public void apply(ResourceManager resources) {
                instance.load(resources);
            }
        });
    }

    public static GlyphRenderManager get() {
        GlyphRenderManager instance = GlyphRenderManager.instance;
        if (instance == null) {
            throw new IllegalStateException("glyph render manager not yet initialized");
        }
        return instance;
    }

    public void render(MinecraftClient client, Matrix4f transformation, Matrix4f projection, float tickDelta) {
        ClientPlayerEntity player = client.player;
        GlyphRenderer glyphRenderer = this.glyphRenderer;
        if (glyphRenderer == null || player == null) {
            return;
        }

        ClientGlyphTracker glyphTracker = ClientGlyphTracker.INSTANCE;
        Collection<ClientGlyph> glyphs = glyphTracker.getGlyphs();
        ClientGlyph drawingGlyph = glyphTracker.getDrawingGlyph();
        if (glyphs.isEmpty() && drawingGlyph == null) {
            return;
        }

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        try (GlyphRenderer.Batcher batcher = glyphRenderer.start(projection)) {
            for (ClientGlyph glyph : glyphs) {
                this.renderGlyph(batcher, glyph, player, cameraPos, transformation, tickDelta);
            }

            if (drawingGlyph != null) {
                this.renderGlyph(batcher, drawingGlyph, player, cameraPos, transformation, tickDelta);
            }
        }
    }

    private void renderGlyph(GlyphRenderer.Batcher batcher, ClientGlyph glyph, ClientPlayerEntity player, Vec3d cameraPos, Matrix4f transformation, float tickDelta) {
        Entity source = glyph.source;
        Vec3d sourcePos = source.getCameraPosVec(tickDelta);

        GlyphRenderData renderData = this.renderData;

        Matrix4fAccess.set(renderData.glyphToWorld, transformation);

        float translationX = (float) (sourcePos.x - cameraPos.x);
        float translationY = (float) (sourcePos.y - cameraPos.y);
        float translationZ = (float) (sourcePos.z - cameraPos.z);
        Matrix4fAccess.translate(renderData.glyphToWorld, translationX, translationY, translationZ);

        renderData.glyphToWorld.multiply(glyph.plane.getRenderGlyphToWorldMatrix());

        renderData.radius = glyph.radius;
        renderData.formProgress = glyph.getFormProgress(player.world.getTime(), tickDelta);
        renderData.red = glyph.getRed(tickDelta);
        renderData.green = glyph.getGreen(tickDelta);
        renderData.blue = glyph.getBlue(tickDelta);
        renderData.shape = glyph.shape;
        renderData.highlightNodes = glyph.source == player;
        renderData.stroke = glyph.stroke;

        batcher.render(renderData, tickDelta);
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
