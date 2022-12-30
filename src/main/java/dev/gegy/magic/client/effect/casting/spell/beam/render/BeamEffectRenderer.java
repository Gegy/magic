package dev.gegy.magic.client.effect.casting.spell.beam.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectTexture;
import dev.gegy.magic.client.render.GeometryBuilder;
import dev.gegy.magic.client.render.gl.GlBinding;
import dev.gegy.magic.client.render.gl.GlGeometry;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public final class BeamEffectRenderer implements AutoCloseable {
    private final BeamWorldShader worldShader;
    private final BeamEndWorldShader cloudWorldShader;
    private final BeamEndWorldShader impactWorldShader;

    private final GlGeometry crossGeometry;
    private final GlGeometry endGeometry;
    private final GlGeometry textureGeometry;

    private final EffectTexture<BeamRenderParameters> texture;
    private final EffectTexture<BeamRenderParameters> cloudTexture;
    private final EffectTexture<BeamRenderParameters> impactTexture;

    private final Batch batch = new Batch();

    private BeamEffectRenderer(
            final BeamWorldShader worldShader, final BeamEndWorldShader cloudWorldShader, final BeamEndWorldShader impactWorldShader,
            final GlGeometry crossGeometry, final GlGeometry endGeometry, final GlGeometry textureGeometry,
            final EffectTexture<BeamRenderParameters> texture,
            final EffectTexture<BeamRenderParameters> cloudTexture, final EffectTexture<BeamRenderParameters> impactTexture
    ) {
        this.worldShader = worldShader;
        this.cloudWorldShader = cloudWorldShader;
        this.impactWorldShader = impactWorldShader;
        this.crossGeometry = crossGeometry;
        this.endGeometry = endGeometry;
        this.textureGeometry = textureGeometry;
        this.texture = texture;
        this.cloudTexture = cloudTexture;
        this.impactTexture = impactTexture;
    }

    public static BeamEffectRenderer create(final ResourceManager resources) throws IOException {
        final BeamWorldShader worldShader = BeamWorldShader.create(resources);
        final BeamEndWorldShader cloudWorldShader = BeamEndWorldShader.create(resources, parameters -> parameters.cloudModelViewProject);
        final BeamEndWorldShader impactWorldShader = BeamEndWorldShader.create(resources, parameters -> parameters.impactModelViewProject);

        final BeamTextureShader textureShader = BeamTextureShader.create(resources);
        final BeamTextureShader cloudTextureShader = BeamTextureShader.createCloud(resources);
        final BeamTextureShader impactTextureShader = BeamTextureShader.createImpact(resources);

        final GlGeometry crossGeometry = uploadCrossGeometry();
        final GlGeometry endGeometry = GeometryBuilder.uploadQuadPos2f(-1.0f, 1.0f);
        final GlGeometry textureGeometry = GeometryBuilder.uploadQuadPos2f(0.0f, 1.0f);

        final EffectTexture<BeamRenderParameters> texture = EffectTexture.create(textureShader, BeamTexture.WIDTH, BeamTexture.HEIGHT);
        final EffectTexture<BeamRenderParameters> cloudTexture = EffectTexture.create(cloudTextureShader, BeamTexture.END_SIZE);
        final EffectTexture<BeamRenderParameters> impactTexture = EffectTexture.create(impactTextureShader, BeamTexture.END_SIZE);

        return new BeamEffectRenderer(
                worldShader, cloudWorldShader, impactWorldShader,
                crossGeometry, endGeometry, textureGeometry,
                texture, cloudTexture, impactTexture
        );
    }

    private static GlGeometry uploadCrossGeometry() {
        return GeometryBuilder.upload(builder -> {
            final double radius = 0.5;
            final double corner = Math.sqrt((radius * radius) / 2.0);

            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            builder.vertex(-corner, -corner, 0.0).uv(0.0f, 0.0f).endVertex();
            builder.vertex(-corner, -corner, 1.0).uv(1.0f, 0.0f).endVertex();
            builder.vertex(corner, corner, 1.0).uv(1.0f, 1.0f).endVertex();
            builder.vertex(corner, corner, 0.0).uv(0.0f, 1.0f).endVertex();
            builder.vertex(corner, -corner, 0.0).uv(0.0f, 0.0f).endVertex();
            builder.vertex(corner, -corner, 1.0).uv(1.0f, 0.0f).endVertex();
            builder.vertex(-corner, corner, 1.0).uv(1.0f, 1.0f).endVertex();
            builder.vertex(-corner, corner, 0.0).uv(0.0f, 1.0f).endVertex();
        });
    }

    public Batch startBatch(final RenderTarget target) {
        final Batch batch = this.batch;
        batch.start(target);
        return batch;
    }

    @Override
    public void close() {
        worldShader.close();
        cloudWorldShader.close();
        impactWorldShader.close();

        crossGeometry.close();
        endGeometry.close();
        textureGeometry.close();

        texture.close();
        cloudTexture.close();
        impactTexture.close();
    }

    public final class Batch implements AutoCloseable {
        private RenderTarget target;

        void start(final RenderTarget target) {
            this.target = target;
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
        }

        public void render(final BeamRenderParameters parameters) {
            renderToTexture(parameters);

            target.bindWrite(true);

            try (final GlGeometry.Binding geometry = crossGeometry.bind()) {
                renderToWorld(parameters, texture, worldShader, geometry);
            }

            try (final GlGeometry.Binding geometry = endGeometry.bind()) {
                renderToWorld(parameters, impactTexture, impactWorldShader, geometry);
                renderToWorld(parameters, cloudTexture, cloudWorldShader, geometry);
            }
        }

        private void renderToTexture(final BeamRenderParameters parameters) {
            try (final GlGeometry.Binding geometry = textureGeometry.bind()) {
                texture.renderWith(parameters, geometry);
                cloudTexture.renderWith(parameters, geometry);
                impactTexture.renderWith(parameters, geometry);
            }
        }

        private void renderToWorld(final BeamRenderParameters parameters, final EffectTexture<BeamRenderParameters> texture, final EffectShader<BeamRenderParameters> shader, final GlGeometry.Binding geometry) {
            try (
                    final EffectTexture.ReadBinding textureBinding = texture.bindRead();
                    final GlBinding shaderBinding = shader.bind(parameters)
            ) {
                geometry.draw();
            }
        }

        @Override
        public void close() {
            VertexBuffer.unbind();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();

            target = null;
        }
    }
}
