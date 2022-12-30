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

    public void set(BeamEffect beam, WorldRenderContext context) {
        float tickDelta = context.tickDelta();

        var spell = beam.spell();
        var plane = this.plane;
        plane.set(spell.transform(), tickDelta);

        float length = beam.getLength(context.tickDelta());
        this.length = length;

        Matrix4f viewProject = this.computeViewProject(spell, context);

        Matrix4f glyphTransform = plane.planeToWorld();

        this.modelViewProject.set(viewProject).mul(glyphTransform);

        this.setCloudModelViewProject(viewProject, glyphTransform);
        this.setImpactModelViewProject(context, viewProject, glyphTransform, length);

        this.red = beam.color().red();
        this.green = beam.color().green();
        this.blue = beam.color().blue();

        float glyphTime = context.world().getGameTime() % DAY_LENGTH;
        this.time = (glyphTime + tickDelta) / 20.0F;
    }

    private void setCloudModelViewProject(Matrix4f viewProject, Matrix4f spellTransform) {
        this.cloudModelViewProject.set(viewProject)
                .mul(spellTransform)
                .translate(0.0F, 0.0F, 0.8F);
    }

    private void setImpactModelViewProject(WorldRenderContext context, Matrix4f viewProject, Matrix4f spellTransform, float length) {
        Vector4f point = this.endPoint.set(0.0F, 0.0F, length, 1.0F).mul(spellTransform);

        this.impactModelViewProject.set(viewProject)
                .translate(point.x(), point.y(), point.z())
                .rotate(context.camera().rotation());
    }

    private Matrix4f computeViewProject(Spell spell, WorldRenderContext context) {
        Matrix4f modelMatrix = context.matrixStack().last().pose();
        Vec3 cameraPos = context.camera().getPosition();

        Vec3 sourcePos = spell.source().getPosition(context.tickDelta());

        return this.viewProject.set(context.projectionMatrix())
                .mul(modelMatrix)
                .translate(
                        (float) (sourcePos.x - cameraPos.x),
                        (float) (sourcePos.y - cameraPos.y),
                        (float) (sourcePos.z - cameraPos.z)
                );
    }
}
