package dev.gegy.magic.client.effect.casting.spell.beam.render;

import dev.gegy.magic.client.effect.casting.spell.beam.BeamEffect;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.spell.Spell;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class BeamRenderParameters {
    private static final long DAY_LENGTH = 24000;

    public final Matrix4f modelViewProject = new Matrix4f();
    public final Matrix4f cloudModelViewProject = new Matrix4f();
    public final Matrix4f impactModelViewProject = new Matrix4f();

    public float red, green, blue;
    public float time;

    public float length;

    private final Matrix4f viewProject = new Matrix4f();
    private final Vector4f endPoint = new Vector4f();

    private final GlyphPlane plane = new GlyphPlane();

    public void set(final BeamEffect beam, final WorldRenderContext context) {
        final float tickDelta = context.tickDelta();

        final Spell spell = beam.spell();
        final GlyphPlane plane = this.plane;
        plane.set(spell.transform(), tickDelta);

        final float length = beam.getLength(context.tickDelta());
        this.length = length;

        final Matrix4f viewProject = computeViewProject(spell, context);

        final Matrix4f glyphTransform = plane.planeToWorld();

        modelViewProject.set(viewProject).mul(glyphTransform);

        setCloudModelViewProject(viewProject, glyphTransform);
        setImpactModelViewProject(context, viewProject, glyphTransform, length);

        red = beam.color().red();
        green = beam.color().green();
        blue = beam.color().blue();

        final float glyphTime = context.world().getGameTime() % DAY_LENGTH;
        time = (glyphTime + tickDelta) / 20.0f;
    }

    private void setCloudModelViewProject(final Matrix4f viewProject, final Matrix4f spellTransform) {
        cloudModelViewProject.set(viewProject)
                .mul(spellTransform)
                .translate(0.0f, 0.0f, 0.8f);
    }

    private void setImpactModelViewProject(final WorldRenderContext context, final Matrix4f viewProject, final Matrix4f spellTransform, final float length) {
        final Vector4f point = endPoint.set(0.0f, 0.0f, length, 1.0f).mul(spellTransform);

        impactModelViewProject.set(viewProject)
                .translate(point.x(), point.y(), point.z())
                .rotate(context.camera().rotation());
    }

    private Matrix4f computeViewProject(final Spell spell, final WorldRenderContext context) {
        final Matrix4f modelMatrix = context.matrixStack().last().pose();
        final Vec3 cameraPos = context.camera().getPosition();

        final Vec3 sourcePos = spell.source().getPosition(context.tickDelta());

        return viewProject.set(context.projectionMatrix())
                .mul(modelMatrix)
                .translate(
                        (float) (sourcePos.x - cameraPos.x),
                        (float) (sourcePos.y - cameraPos.y),
                        (float) (sourcePos.z - cameraPos.z)
                );
    }
}
