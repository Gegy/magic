package dev.gegy.magic.client.effect.casting.spell.beam;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.math.Constants;
import dev.gegy.magic.client.effect.EffectSelector;
import dev.gegy.magic.client.effect.EffectSystem;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamEffectRenderer;
import dev.gegy.magic.client.effect.casting.spell.beam.render.BeamRenderParameters;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.particle.MagicParticles;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

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

    private BeamEffectSystem(final BeamEffectRenderer renderer) {
        this.renderer = renderer;
    }

    public static BeamEffectSystem create(final ResourceManager resources) throws IOException {
        final BeamEffectRenderer renderer = BeamEffectRenderer.create(resources);
        return new BeamEffectSystem(renderer);
    }

    @Override
    public void render(final Minecraft client, final WorldRenderContext context, final RenderTarget targetFramebuffer, final EffectSelector effects) {
        final List<BeamEffect> visibleBeams = collectVisibleBeams(effects);
        if (visibleBeams.isEmpty()) {
            return;
        }

        try (final BeamEffectRenderer.Batch batch = renderer.startBatch(targetFramebuffer)) {
            final BeamRenderParameters parameters = this.parameters;
            for (final BeamEffect beam : visibleBeams) {
                parameters.set(beam, context);
                batch.render(parameters);
            }
        } finally {
            visibleBeams.clear();
        }
    }

    private List<BeamEffect> collectVisibleBeams(final EffectSelector effects) {
        final List<BeamEffect> beams = visibleBeams;
        for (final BeamEffect beam : effects.select(BeamEffect.TYPE)) {
            if (beam.visible()) {
                beams.add(beam);
            }
        }
        return beams;
    }

    @Override
    public void tick(final Minecraft client, final EffectSelector effects) {
        for (final BeamEffect beam : effects.select(BeamEffect.TYPE)) {
            if (beam.visible()) {
                spawnParticles(client.particleEngine, beam);
            }
        }
    }

    private void spawnParticles(final ParticleEngine particleManager, final BeamEffect beam) {
        final Spell spell = beam.spell();

        final GlyphPlane plane = this.plane;
        plane.set(spell.transform());

        final Vec3 sourcePos = spell.source().getPosition(1.0f);

        spawnBeamParticles(particleManager, sourcePos, plane);
        spawnImpactParticles(particleManager, beam, sourcePos, plane);
    }

    private void spawnBeamParticles(final ParticleEngine particleManager, final Vec3 sourcePos, final GlyphPlane plane) {
        final float radius = 0.5f + random.nextFloat() * 0.25f;
        final float theta = random.nextFloat() * 2.0f * Constants.PI;

        final Vector3f origin = this.plane.projectToWorld(
                Mth.sin(theta) * radius,
                Mth.cos(theta) * radius
        );

        final Vector3f direction = plane.direction();
        final double velocityX = direction.x() * 0.2 + random.nextGaussian() * 0.01;
        final double velocityY = direction.y() * 0.2 + random.nextGaussian() * 0.01;
        final double velocityZ = direction.z() * 0.2 + random.nextGaussian() * 0.01;

        // TODO: despawn based on distance from beam

        particleManager.createParticle(
                MagicParticles.SPARK,
                sourcePos.x + origin.x(),
                sourcePos.y + origin.y(),
                sourcePos.z + origin.z(),
                velocityX, velocityY, velocityZ
        );
    }

    private void spawnImpactParticles(final ParticleEngine particleManager, final BeamEffect beam, final Vec3 sourcePos, final GlyphPlane plane) {
        final float length = beam.getLength(1.0f);
        final Vector3f origin = plane.projectToWorld(0.0f, 0.0f, length);

        final float theta = random.nextFloat() * 2.0f * Constants.PI;

        final Vector3f ejectVelocity = plane.projectToWorld(
                Mth.sin(theta) * 0.5f,
                Mth.cos(theta) * 0.5f,
                0.0f
        ).mul(0.2f);

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
        renderer.close();
    }
}
