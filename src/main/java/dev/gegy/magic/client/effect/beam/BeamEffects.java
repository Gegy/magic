package dev.gegy.magic.client.effect.beam;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.gegy.magic.client.effect.shader.EffectShader;
import dev.gegy.magic.client.effect.shader.EffectTexture;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.particle.MagicParticles;
import dev.gegy.magic.client.render.GeometryBuilder;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathConstants;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.io.IOException;
import java.util.Random;

public final class BeamEffects implements AutoCloseable {
    private final BeamWorldShader worldShader;
    private final BeamEndWorldShader cloudWorldShader;
    private final BeamEndWorldShader impactWorldShader;

    private final VertexBuffer crossGeometry;
    private final VertexBuffer endGeometry;
    private final VertexBuffer textureGeometry;

    private final EffectTexture<BeamRenderParameters> texture;
    private final EffectTexture<BeamRenderParameters> cloudTexture;
    private final EffectTexture<BeamRenderParameters> impactTexture;

    private final Renderer renderer = new Renderer();

    private final Random random = new Random();

    private BeamEffects(
            BeamWorldShader worldShader, BeamEndWorldShader cloudWorldShader, BeamEndWorldShader impactWorldShader,
            VertexBuffer crossGeometry, VertexBuffer endGeometry, VertexBuffer textureGeometry,
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

    public static BeamEffects create(ResourceManager resources) throws IOException {
        BeamWorldShader worldShader = BeamWorldShader.create(resources);
        BeamEndWorldShader cloudWorldShader = BeamEndWorldShader.create(resources, parameters -> parameters.cloudModelViewProject);
        BeamEndWorldShader impactWorldShader = BeamEndWorldShader.create(resources, parameters -> parameters.impactModelViewProject);

        BeamTextureShader textureShader = BeamTextureShader.create(resources);
        BeamTextureShader cloudTextureShader = BeamTextureShader.createCloud(resources);
        BeamTextureShader impactTextureShader = BeamTextureShader.createImpact(resources);

        VertexBuffer crossGeometry = uploadCrossGeometry();
        VertexBuffer endGeometry = GeometryBuilder.uploadQuadPos2f(-1.0F, 1.0F);
        VertexBuffer textureGeometry = GeometryBuilder.uploadQuadPos2f(0.0F, 1.0F);

        EffectTexture<BeamRenderParameters> texture = EffectTexture.create(textureShader, BeamTexture.WIDTH, BeamTexture.HEIGHT);
        EffectTexture<BeamRenderParameters> cloudTexture = EffectTexture.create(cloudTextureShader, BeamTexture.END_SIZE);
        EffectTexture<BeamRenderParameters> impactTexture = EffectTexture.create(impactTextureShader, BeamTexture.END_SIZE);

        return new BeamEffects(
                worldShader, cloudWorldShader, impactWorldShader,
                crossGeometry, endGeometry, textureGeometry,
                texture, cloudTexture, impactTexture
        );
    }

    private static VertexBuffer uploadCrossGeometry() {
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

    public Renderer renderTo(Framebuffer target) {
        Renderer renderer = this.renderer;
        renderer.start(target);
        return renderer;
    }

    public void spawnParticles(ParticleManager particleManager, ClientGlyph glyph) {
        float distance = glyph.transform.getDistance(1.0F);
        Vec3f direction = glyph.transform.getDirection(1.0F);

        Vec3d sourcePos = glyph.source.getCameraPosVec(1.0F);

        this.spawnBeamParticles(particleManager, glyph, sourcePos, direction, distance);
        this.spawnImpactParticles(particleManager, glyph, sourcePos, distance);
    }

    private void spawnBeamParticles(ParticleManager particleManager, ClientGlyph glyph, Vec3d sourcePos, Vec3f direction, float distance) {
        float radius = glyph.radius * 0.5F + this.random.nextFloat() * 0.25F;
        float theta = this.random.nextFloat() * 2.0F * MathConstants.PI;

        Vec3f origin = glyph.transform.projectFromPlane(
                MathHelper.sin(theta) * radius,
                MathHelper.cos(theta) * radius,
                distance + glyph.radius
        );

        double velocityX = direction.getX() * 0.2 + this.random.nextGaussian() * 0.01;
        double velocityY = direction.getY() * 0.2 + this.random.nextGaussian() * 0.01;
        double velocityZ = direction.getZ() * 0.2 + this.random.nextGaussian() * 0.01;

        // TODO: despawn based on distance from beam

        particleManager.addParticle(
                MagicParticles.SPARK,
                sourcePos.x + origin.getX(),
                sourcePos.y + origin.getY(),
                sourcePos.z + origin.getZ(),
                velocityX, velocityY, velocityZ
        );
    }

    private void spawnImpactParticles(ParticleManager particleManager, ClientGlyph glyph, Vec3d sourcePos, float distance) {
        Vec3f origin = new Vec3f(0.0F, 0.0F, distance + 16.0F * glyph.radius); // TODO: length
        glyph.transform.projectFromPlane(origin);

        float theta = this.random.nextFloat() * 2.0F * MathConstants.PI;

        Vec3f ejectVelocity = glyph.transform.projectFromPlane(
                MathHelper.sin(theta) * 0.5F,
                MathHelper.cos(theta) * 0.5F,
                0.0F
        );
        ejectVelocity.scale(0.2F);

        particleManager.addParticle(
                MagicParticles.SPARK,
                sourcePos.x + origin.getX(),
                sourcePos.y + origin.getY(),
                sourcePos.z + origin.getZ(),
                ejectVelocity.getX(), ejectVelocity.getY(), ejectVelocity.getZ()
        );
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

    public final class Renderer implements AutoCloseable {
        private Framebuffer target;

        void start(Framebuffer target) {
            this.target = target;
            RenderSystem.disableCull();
        }

        public void render(BeamRenderParameters parameters) {
            this.renderToTexture(parameters);

            this.target.beginWrite(true);

            this.renderToWorld(parameters, BeamEffects.this.texture, BeamEffects.this.worldShader, BeamEffects.this.crossGeometry);
            this.renderToWorld(parameters, BeamEffects.this.impactTexture, BeamEffects.this.impactWorldShader, BeamEffects.this.endGeometry);
            this.renderToWorld(parameters, BeamEffects.this.cloudTexture, BeamEffects.this.cloudWorldShader, BeamEffects.this.endGeometry);
        }

        private void renderToTexture(BeamRenderParameters parameters) {
            VertexBuffer geometry = BeamEffects.this.textureGeometry;
            VertexFormat format = geometry.getElementFormat();

            geometry.bind();
            format.startDrawing();

            BeamEffects.this.texture.renderWith(parameters, geometry);
            BeamEffects.this.cloudTexture.renderWith(parameters, geometry);
            BeamEffects.this.impactTexture.renderWith(parameters, geometry);

            format.endDrawing();
        }

        private void renderToWorld(BeamRenderParameters parameters, EffectTexture<BeamRenderParameters> texture, EffectShader<BeamRenderParameters> shader, VertexBuffer geometry) {
            VertexFormat format = geometry.getElementFormat();

            geometry.bind();
            format.startDrawing();

            shader.bind(parameters);
            texture.bindRead();

            geometry.drawElements();

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
