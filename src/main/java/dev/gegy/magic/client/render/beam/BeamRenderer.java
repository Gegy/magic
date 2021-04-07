package dev.gegy.magic.client.render.beam;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.render.MagicGeometry;
import dev.gegy.magic.client.render.shader.EffectTexture;
import dev.gegy.magic.client.render.shader.RenderEffect;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;

import java.io.IOException;

public final class BeamRenderer implements AutoCloseable {
    private final BeamWorldEffect worldEffect;
    private final BeamEndWorldEffect cloudWorldEffect;
    private final BeamEndWorldEffect impactWorldEffect;

    private final VertexBuffer crossGeometry;
    private final VertexBuffer endGeometry;
    private final VertexBuffer textureGeometry;

    private final EffectTexture<BeamRenderData> texture;
    private final EffectTexture<BeamRenderData> cloudTexture;
    private final EffectTexture<BeamRenderData> impactTexture;

    private final Batcher batcher = new Batcher();

    private BeamRenderer(
            BeamWorldEffect worldEffect, BeamEndWorldEffect cloudWorldEffect, BeamEndWorldEffect impactWorldEffect,
            VertexBuffer crossGeometry, VertexBuffer endGeometry, VertexBuffer textureGeometry,
            EffectTexture<BeamRenderData> texture,
            EffectTexture<BeamRenderData> cloudTexture, EffectTexture<BeamRenderData> impactTexture
    ) {
        this.worldEffect = worldEffect;
        this.cloudWorldEffect = cloudWorldEffect;
        this.impactWorldEffect = impactWorldEffect;
        this.crossGeometry = crossGeometry;
        this.endGeometry = endGeometry;
        this.textureGeometry = textureGeometry;
        this.texture = texture;
        this.cloudTexture = cloudTexture;
        this.impactTexture = impactTexture;
    }

    public static BeamRenderer create(ResourceManager resources) throws IOException {
        BeamWorldEffect worldEffect = BeamWorldEffect.create(resources);
        BeamEndWorldEffect cloudWorldEffect = BeamEndWorldEffect.create(resources, data -> data.cloudModelViewProject);
        BeamEndWorldEffect impactWorldEffect = BeamEndWorldEffect.create(resources, data -> data.impactModelViewProject);

        BeamTextureEffect textureEffect = BeamTextureEffect.create(resources);
        BeamTextureEffect cloudTextureEffect = BeamTextureEffect.createCloud(resources);
        BeamTextureEffect endTextureEffect = BeamTextureEffect.createImpact(resources);

        VertexBuffer crossGeometry = uploadCrossGeometry();
        VertexBuffer endGeometry = MagicGeometry.uploadQuadPos2f(-1.0F, 1.0F);
        VertexBuffer textureGeometry = MagicGeometry.uploadQuadPos2f(0.0F, 1.0F);

        EffectTexture<BeamRenderData> texture = EffectTexture.create(textureEffect, BeamTexture.WIDTH, BeamTexture.HEIGHT);
        EffectTexture<BeamRenderData> cloudTexture = EffectTexture.create(cloudTextureEffect, BeamTexture.END_SIZE);
        EffectTexture<BeamRenderData> endTexture = EffectTexture.create(endTextureEffect, BeamTexture.END_SIZE);

        return new BeamRenderer(
                worldEffect, cloudWorldEffect, impactWorldEffect,
                crossGeometry, endGeometry, textureGeometry,
                texture, cloudTexture, endTexture
        );
    }

    private static VertexBuffer uploadCrossGeometry() {
        return MagicGeometry.upload(builder -> {
            double radius = 0.5;
            double corner = Math.sqrt((radius * radius) / 2.0);

            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
            builder.vertex(-corner, -corner, 0.0).texture(0.0F, 0.0F).next();
            builder.vertex(-corner, -corner, 1.0).texture(1.0F, 0.0F).next();
            builder.vertex(corner, corner, 1.0).texture(1.0F, 1.0F).next();
            builder.vertex(corner, corner, 0.0).texture(0.0F, 1.0F).next();
            builder.vertex(corner, -corner, 0.0).texture(0.0F, 0.0F).next();
            builder.vertex(corner, -corner, 1.0).texture(1.0F, 0.0F).next();
            builder.vertex(-corner, corner, 1.0).texture(1.0F, 1.0F).next();
            builder.vertex(-corner, corner, 0.0).texture(0.0F, 1.0F).next();
        });
    }

    public Batcher start(Framebuffer target) {
        Batcher batcher = this.batcher;
        batcher.start(target);
        return batcher;
    }

    @Override
    public void close() {
        this.worldEffect.close();
        this.cloudWorldEffect.close();
        this.impactWorldEffect.close();

        this.crossGeometry.close();
        this.endGeometry.close();
        this.textureGeometry.close();

        this.texture.close();
        this.cloudTexture.close();
        this.impactTexture.close();
    }

    public final class Batcher implements AutoCloseable {
        private Framebuffer target;

        void start(Framebuffer target) {
            this.target = target;
            RenderSystem.disableCull();
        }

        public void render(BeamRenderData renderData) {
            this.renderToTexture(renderData);

            this.target.beginWrite(true);

            this.renderToWorld(renderData, BeamRenderer.this.texture, BeamRenderer.this.worldEffect, BeamRenderer.this.crossGeometry);
            this.renderToWorld(renderData, BeamRenderer.this.impactTexture, BeamRenderer.this.impactWorldEffect, BeamRenderer.this.endGeometry);
            this.renderToWorld(renderData, BeamRenderer.this.cloudTexture, BeamRenderer.this.cloudWorldEffect, BeamRenderer.this.endGeometry);
        }

        private void renderToTexture(BeamRenderData renderData) {
            BeamRenderer.this.textureGeometry.bind();
            MagicGeometry.POSITION_2F.startDrawing();

            VertexBuffer geometry = BeamRenderer.this.textureGeometry;
            BeamRenderer.this.texture.renderWith(renderData, geometry);
            BeamRenderer.this.cloudTexture.renderWith(renderData, geometry);
            BeamRenderer.this.impactTexture.renderWith(renderData, geometry);

            MagicGeometry.POSITION_2F.endDrawing();
        }

        private void renderToWorld(BeamRenderData renderData, EffectTexture<BeamRenderData> texture, RenderEffect<BeamRenderData> shader, VertexBuffer geometry) {
            VertexFormat format = geometry.method_34435();

            geometry.bind();
            format.startDrawing();

            shader.bind(renderData);
            texture.bindRead();

            geometry.method_35665();

            texture.unbindRead();
            shader.unbind();

            format.endDrawing();
        }

        @Override
        public void close() {
            VertexBuffer.unbind();
            RenderSystem.enableCull();

            this.target = null;
        }
    }
}
