package dev.gegy.magic.client.effect.casting.spell.beam.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectTexture;
import dev.gegy.magic.client.render.GeometryBuilder;
import dev.gegy.magic.client.render.gl.GlGeometry;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;

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
            BeamWorldShader worldShader, BeamEndWorldShader cloudWorldShader, BeamEndWorldShader impactWorldShader,
            GlGeometry crossGeometry, GlGeometry endGeometry, GlGeometry textureGeometry,
            EffectTexture<BeamRenderParameters> texture,
            EffectTexture<BeamRenderParameters> cloudTexture, EffectTexture<BeamRenderParameters> impactTexture
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

    public static BeamEffectRenderer create(ResourceManager resources) throws IOException {
        var worldShader = BeamWorldShader.create(resources);
        var cloudWorldShader = BeamEndWorldShader.create(resources, parameters -> parameters.cloudModelViewProject);
        var impactWorldShader = BeamEndWorldShader.create(resources, parameters -> parameters.impactModelViewProject);

        var textureShader = BeamTextureShader.create(resources);
        var cloudTextureShader = BeamTextureShader.createCloud(resources);
        var impactTextureShader = BeamTextureShader.createImpact(resources);

        var crossGeometry = uploadCrossGeometry();
        var endGeometry = GeometryBuilder.uploadQuadPos2f(-1.0F, 1.0F);
        var textureGeometry = GeometryBuilder.uploadQuadPos2f(0.0F, 1.0F);

        var texture = EffectTexture.create(textureShader, BeamTexture.WIDTH, BeamTexture.HEIGHT);
        var cloudTexture = EffectTexture.create(cloudTextureShader, BeamTexture.END_SIZE);
        var impactTexture = EffectTexture.create(impactTextureShader, BeamTexture.END_SIZE);

        return new BeamEffectRenderer(
                worldShader, cloudWorldShader, impactWorldShader,
                crossGeometry, endGeometry, textureGeometry,
                texture, cloudTexture, impactTexture
        );
    }

    private static GlGeometry uploadCrossGeometry() {
        return GeometryBuilder.upload(builder -> {
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

    public Batch startBatch(Framebuffer target) {
        var batch = this.batch;
        batch.start(target);
        return batch;
    }

    @Override
    public void close() {
        this.worldShader.close();
        this.cloudWorldShader.close();
        this.impactWorldShader.close();

        this.crossGeometry.close();
        this.endGeometry.close();
        this.textureGeometry.close();

        this.texture.close();
        this.cloudTexture.close();
        this.impactTexture.close();
    }

    public final class Batch implements AutoCloseable {
        private Framebuffer target;

        void start(Framebuffer target) {
            this.target = target;
            RenderSystem.disableCull();
            RenderSystem.enableDepthTest();
        }

        public void render(BeamRenderParameters parameters) {
            this.renderToTexture(parameters);

            this.target.beginWrite(true);

            try (var geometry = BeamEffectRenderer.this.crossGeometry.bind()) {
                this.renderToWorld(parameters, BeamEffectRenderer.this.texture, BeamEffectRenderer.this.worldShader, geometry);
            }

            try (var geometry = BeamEffectRenderer.this.endGeometry.bind()) {
                this.renderToWorld(parameters, BeamEffectRenderer.this.impactTexture, BeamEffectRenderer.this.impactWorldShader, geometry);
                this.renderToWorld(parameters, BeamEffectRenderer.this.cloudTexture, BeamEffectRenderer.this.cloudWorldShader, geometry);
            }
        }

        private void renderToTexture(BeamRenderParameters parameters) {
            try (var geometry = BeamEffectRenderer.this.textureGeometry.bind()) {
                BeamEffectRenderer.this.texture.renderWith(parameters, geometry);
                BeamEffectRenderer.this.cloudTexture.renderWith(parameters, geometry);
                BeamEffectRenderer.this.impactTexture.renderWith(parameters, geometry);
            }
        }

        private void renderToWorld(BeamRenderParameters parameters, EffectTexture<BeamRenderParameters> texture, EffectShader<BeamRenderParameters> shader, GlGeometry.Binding geometry) {
            try (
                    var textureBinding = texture.bindRead();
                    var shaderBinding = shader.bind(parameters)
            ) {
                geometry.draw();
            }
        }

        @Override
        public void close() {
            VertexBuffer.unbind();
            RenderSystem.enableCull();
            RenderSystem.disableDepthTest();

            this.target = null;
        }
    }
}
