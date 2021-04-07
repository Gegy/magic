package dev.gegy.magic.client.render.glyph;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.GlyphColor;
import dev.gegy.magic.client.glyph.GlyphStroke;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public final class GlyphRenderData {
    public final Matrix4f modelViewProject = new Matrix4f();
    public float distance;
    public float radius;
    public float formProgress;
    public float primaryRed, primaryGreen, primaryBlue;
    public float secondaryRed, secondaryGreen, secondaryBlue;
    public int shape;

    public float tickDelta;

    public boolean highlightNodes;
    public GlyphStroke stroke;

    public void set(ClientGlyph glyph, WorldRenderContext context) {
        Matrix4f modelMatrix = context.matrixStack().peek().getModel();
        Vec3d cameraPos = context.camera().getPos();
        float tickDelta = context.tickDelta();

        Entity source = glyph.source;
        Vec3d sourcePos = source.getCameraPosVec(tickDelta);

        Matrix4f modelViewProject = this.modelViewProject;
        modelViewProject.method_35434(context.projectionMatrix());
        modelViewProject.multiply(modelMatrix);
        modelViewProject.multiplyByTranslation(
                (float) (sourcePos.x - cameraPos.x),
                (float) (sourcePos.y - cameraPos.y),
                (float) (sourcePos.z - cameraPos.z)
        );
        modelViewProject.multiply(glyph.transform.getTransformationMatrix(tickDelta));

        this.distance = glyph.transform.getDistance(tickDelta);
        this.radius = glyph.radius;
        this.formProgress = glyph.getFormProgress(context.world().getTime(), tickDelta);

        GlyphColor primaryColor = glyph.getPrimaryColor();
        this.primaryRed = primaryColor.getRed(tickDelta);
        this.primaryGreen = primaryColor.getGreen(tickDelta);
        this.primaryBlue = primaryColor.getBlue(tickDelta);

        GlyphColor secondaryColor = glyph.getSecondaryColor();
        this.secondaryRed = secondaryColor.getRed(tickDelta);
        this.secondaryGreen = secondaryColor.getGreen(tickDelta);
        this.secondaryBlue = secondaryColor.getBlue(tickDelta);

        this.tickDelta = tickDelta;

        this.shape = glyph.shape;
        this.highlightNodes = glyph.source == context.camera().getFocusedEntity();
        this.stroke = glyph.stroke;
    }
}
