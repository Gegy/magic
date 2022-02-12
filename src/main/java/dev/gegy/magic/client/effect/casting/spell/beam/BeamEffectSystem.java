package dev.gegy.magic.client.effect.casting.spell.beam;

import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamEffectRenderer;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamRenderParameters;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.particle.MagicParticles;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.math.MathConstants;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class BeamEffectSystem implements EffectSystem {
    private final BeamEffectRenderer renderer;
    private final BeamRenderParameters parameters = new BeamRenderParameters();

    private final Random random = new Random();

    private final GlyphPlane plane = new GlyphPlane();

    private final List<BeamEffect> visibleBeams = new ArrayList<>();

    private BeamEffectSystem(BeamEffectRenderer renderer) {
        this.renderer = renderer;
    }

    public static BeamEffectSystem create(ResourceManager resources) throws IOException {
        var renderer = BeamEffectRenderer.create(resources);
        return new BeamEffectSystem(renderer);
    }

    @Override
    public void render(MinecraftClient client, WorldRenderContext context, Framebuffer targetFramebuffer, EffectSelector effects) {
        var visibleBeams = this.collectVisibleBeams(effects);
        if (visibleBeams.isEmpty()) {
            return;
        }

        try (var batch = this.renderer.startBatch(targetFramebuffer)) {
            var parameters = this.parameters;
            for (var beam : visibleBeams) {
                parameters.set(beam, context);
                batch.render(parameters);
            }
        } finally {
            visibleBeams.clear();
        }
    }

    private List<BeamEffect> collectVisibleBeams(EffectSelector effects) {
        var beams = this.visibleBeams;
        for (var beam : effects.select(BeamEffect.TYPE)) {
            if (beam.visible()) {
                beams.add(beam);
            }
        }
        return beams;
    }

    @Override
    public void tick(MinecraftClient client, EffectSelector effects) {
        for (var beam : effects.select(BeamEffect.TYPE)) {
            if (beam.visible()) {
                this.spawnParticles(client.particleManager, beam);
            }
        }
    }

    private void spawnParticles(ParticleManager particleManager, BeamEffect beam) {
        var spell = beam.spell();

        var plane = this.plane;
        plane.set(spell.transform());

        var sourcePos = spell.source().getPosition(1.0F);

        this.spawnBeamParticles(particleManager, sourcePos, plane);
        this.spawnImpactParticles(particleManager, beam, sourcePos, plane);
    }

    private void spawnBeamParticles(ParticleManager particleManager, Vec3d sourcePos, GlyphPlane plane) {
        float radius = 0.5F + this.random.nextFloat() * 0.25F;
        float theta = this.random.nextFloat() * 2.0F * MathConstants.PI;

        var origin = this.plane.projectToWorld(
                MathHelper.sin(theta) * radius,
                MathHelper.cos(theta) * radius
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
        var origin = plane.projectToWorld(0.0F, 0.0F, length);

        float theta = this.random.nextFloat() * 2.0F * MathConstants.PI;

        var ejectVelocity = plane.projectToWorld(
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
