package dev.gegy.magic.client.effect.casting.spell.beam;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.math.Constants;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamEffectRenderer;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamRenderParameters;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.particle.MagicParticles;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

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
    public void render(Minecraft client, WorldRenderContext context, RenderTarget targetFramebuffer, EffectSelector effects) {
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
    public void tick(Minecraft client, EffectSelector effects) {
        for (var beam : effects.select(BeamEffect.TYPE)) {
            if (beam.visible()) {
                this.spawnParticles(client.particleEngine, beam);
            }
        }
    }

    private void spawnParticles(ParticleEngine particleManager, BeamEffect beam) {
        var spell = beam.spell();

        var plane = this.plane;
        plane.set(spell.transform());

        var sourcePos = spell.source().getPosition(1.0F);

        this.spawnBeamParticles(particleManager, sourcePos, plane);
        this.spawnImpactParticles(particleManager, beam, sourcePos, plane);
    }

    private void spawnBeamParticles(ParticleEngine particleManager, Vec3 sourcePos, GlyphPlane plane) {
        float radius = 0.5F + this.random.nextFloat() * 0.25F;
        float theta = this.random.nextFloat() * 2.0F * Constants.PI;

        var origin = this.plane.projectToWorld(
                Mth.sin(theta) * radius,
                Mth.cos(theta) * radius
        );

        var direction = plane.direction();
        double velocityX = direction.x() * 0.2 + this.random.nextGaussian() * 0.01;
        double velocityY = direction.y() * 0.2 + this.random.nextGaussian() * 0.01;
        double velocityZ = direction.z() * 0.2 + this.random.nextGaussian() * 0.01;

        // TODO: despawn based on distance from beam

        particleManager.createParticle(
                MagicParticles.SPARK,
                sourcePos.x + origin.x(),
                sourcePos.y + origin.y(),
                sourcePos.z + origin.z(),
                velocityX, velocityY, velocityZ
        );
    }

    private void spawnImpactParticles(ParticleEngine particleManager, BeamEffect beam, Vec3 sourcePos, GlyphPlane plane) {
        var length = beam.getLength(1.0F);
        var origin = plane.projectToWorld(0.0F, 0.0F, length);

        float theta = this.random.nextFloat() * 2.0F * Constants.PI;

        var ejectVelocity = plane.projectToWorld(
                Mth.sin(theta) * 0.5F,
                Mth.cos(theta) * 0.5F,
                0.0F
        ).mul(0.2F);

        particleManager.createParticle(
                MagicParticles.SPARK,
                sourcePos.x + origin.x(),
                sourcePos.y + origin.y(),
                sourcePos.z + origin.z(),
                ejectVelocity.x(), ejectVelocity.y(), ejectVelocity.z()
        );
    }

    @Override
    public void close() {
        this.renderer.close();
    }
}
