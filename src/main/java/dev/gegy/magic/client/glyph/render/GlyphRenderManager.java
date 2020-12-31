package dev.gegy.magic.client.glyph.render;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.Matrix4fAccess;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.glyph.Glyph;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.Set;

public final class GlyphRenderManager {
    private static GlyphRenderManager instance;

    private GlyphRenderer glyphRenderer;

    private final Matrix4f glyphToWorld = new Matrix4f();

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

    public void render(MinecraftClient client, Matrix4f transformation, Matrix4f projection) {
        GlyphRenderer glyphRenderer = this.glyphRenderer;
        if (glyphRenderer == null) {
            return;
        }

        Set<Glyph> glyphs = ClientGlyphTracker.INSTANCE.getGlyphs();
        if (glyphs.isEmpty()) {
            return;
        }

        Camera camera = client.gameRenderer.getCamera();
        Vec3d cameraPos = camera.getPos();

        Matrix4f glyphToWorld = this.glyphToWorld;

        try (GlyphRenderer.Batcher batcher = glyphRenderer.start(projection)) {
            for (Glyph glyph : glyphs) {
                Vec3d source = glyph.source;

                Matrix4fAccess.set(glyphToWorld, transformation);

                float translationX = (float) (source.x - cameraPos.x);
                float translationY = (float) (source.y - cameraPos.y);
                float translationZ = (float) (source.z - cameraPos.z);
                Matrix4fAccess.translate(glyphToWorld, translationX, translationY, translationZ);

                glyphToWorld.multiply(glyph.glyphToWorld);

                batcher.render(glyphToWorld, glyph.centerX, glyph.centerY, glyph.radius, glyph.red, glyph.green, glyph.blue, 0b1011011);
            }
        }
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
