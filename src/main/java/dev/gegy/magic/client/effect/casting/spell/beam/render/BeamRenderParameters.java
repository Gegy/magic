package dev.gegy.magic.client.effect.casting.spell.beam.render;

import dev.gegy.magic.client.effect.casting.spell.beam.BeamEffect;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.spell.Spell;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vector4f;

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

        Matrix4f glyphTransform = plane.getPlaneToWorldMatrix();

        Matrix4f modelViewProject = this.modelViewProject;
        modelViewProject.load(viewProject);
        modelViewProject.multiply(glyphTransform);

        this.setCloudModelViewProject(viewProject, glyphTransform);
        this.setImpactModelViewProject(context, viewProject, glyphTransform, length);

        this.red = beam.color().red();
        this.green = beam.color().green();
        this.blue = beam.color().blue();

        float glyphTime = context.world().getTime() % DAY_LENGTH;
        this.time = (glyphTime + tickDelta) / 20.0F;
    }

    private void setCloudModelViewProject(Matrix4f viewProject, Matrix4f spellTransform) {
        Matrix4f cloudModelViewProject = this.cloudModelViewProject;
        cloudModelViewProject.load(viewProject);
        cloudModelViewProject.multiply(spellTransform);
        cloudModelViewProject.multiplyByTranslation(0.0F, 0.0F, 0.8F);
    }

    private void setImpactModelViewProject(WorldRenderContext context, Matrix4f viewProject, Matrix4f spellTransform, float length) {
        Vector4f point = this.endPoint;
        point.set(0.0F, 0.0F, length, 1.0F);
        point.transform(spellTransform);

        Matrix4f impactModelViewProject = this.impactModelViewProject;

        impactModelViewProject.load(viewProject);
        impactModelViewProject.multiplyByTranslation(point.getX(), point.getY(), point.getZ());
        impactModelViewProject.multiply(context.camera().getRotation());
    }

    private Matrix4f computeViewProject(Spell spell, WorldRenderContext context) {
        Matrix4f modelMatrix = context.matrixStack().peek().getPositionMatrix();
        Vec3d cameraPos = context.camera().getPos();

        Vec3d sourcePos = spell.source().getPosition(context.tickDelta());

        Matrix4f viewProject = this.viewProject;
        viewProject.load(context.projectionMatrix());
        viewProject.multiply(modelMatrix);
        viewProject.multiplyByTranslation(
                (float) (sourcePos.x - cameraPos.x),
                (float) (sourcePos.y - cameraPos.y),
                (float) (sourcePos.z - cameraPos.z)
        );

        return viewProject;
    }
}
