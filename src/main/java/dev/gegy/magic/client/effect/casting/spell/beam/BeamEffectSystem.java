package dev.gegy.magic.client.effect.casting.spell.beam;

import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamEffectRenderer;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamRenderParameters;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.particle.MagicParticles;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathConstants;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.Random;

public final class BeamEffectSystem implements EffectSystem {
    private final BeamEffectRenderer renderer;
    private final BeamRenderParameters parameters = new BeamRenderParameters();

    private final Random random = new Random();

    private final GlyphPlane plane = new GlyphPlane();

    private BeamEffectSystem(BeamEffectRenderer renderer) {
        this.renderer = renderer;
    }

    public static BeamEffectSystem create(ResourceManager resources) throws IOException {
        var renderer = BeamEffectRenderer.create(resources);
        return new BeamEffectSystem(renderer);
    }

    @Override
    public void render(MinecraftClient client, WorldRenderContext context, EffectSelector effects) {
        var beams = effects.select(BeamEffect.TYPE);
        if (beams.isEmpty()) {
            return;
        }

        try (var batch = this.renderer.startBatch(client.getFramebuffer())) {
            var parameters = this.parameters;
            for (var beam : beams) {
                parameters.set(beam, context);
                batch.render(parameters);
            }
        }
    }

    @Override
    public void tick(MinecraftClient client, EffectSelector effects) {
        for (var beam : effects.select(BeamEffect.TYPE)) {
            this.spawnParticles(client.particleManager, beam);
        }
    }

    private void spawnParticles(ParticleManager particleManager, BeamEffect beam) {
        var spell = beam.spell();

        var plane = this.plane;
        plane.set(spell.transform());

        var sourcePos = spell.source().getPosition(1.0F);

        this.spawnBeamParticles(particleManager, sourcePos, plane, beam.scale());
        this.spawnImpactParticles(particleManager, beam, sourcePos, plane);
    }

    private void spawnBeamParticles(ParticleManager particleManager, Vec3d sourcePos, GlyphPlane plane, float scale) {
        float radius = scale * 0.5F + this.random.nextFloat() * 0.25F;
        float theta = this.random.nextFloat() * 2.0F * MathConstants.PI;

        var origin = this.plane.projectFromPlane(
                MathHelper.sin(theta) * radius,
                MathHelper.cos(theta) * radius,
                plane.getDistance()
        );

        var direction = plane.getDirection();
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

    private void spawnImpactParticles(ParticleManager particleManager, BeamEffect beam, Vec3d sourcePos, GlyphPlane plane) {
        var length = beam.getLength(1.0F);
        var origin = plane.projectFromPlane(0.0F, 0.0F, plane.getDistance() + length);

        float theta = this.random.nextFloat() * 2.0F * MathConstants.PI;

        var ejectVelocity =  plane.projectFromPlane(
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
        this.renderer.close();
    }
}
