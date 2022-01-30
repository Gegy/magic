package dev.gegy.magic.client.effect.glyph;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.casting.drawing.FadingGlyph;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.PreparedGlyph;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.GlyphForm;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;

public final class GlyphRenderParameters {
    public final Matrix4f modelViewProject = new Matrix4f();
    public float distance;
    public float radius;
    public float opacity;
    public float primaryRed, primaryGreen, primaryBlue;
    public float secondaryRed, secondaryGreen, secondaryBlue;
    public int shape;

    public boolean highlightNodes;

    @Nullable
    public GlyphStroke stroke;

    private final GlyphPlane plane = new GlyphPlane();

    public void setDrawing(ClientDrawingGlyph glyph, WorldRenderContext context) {
        float tickDelta = context.tickDelta();

        this.setTransform(glyph.source(), glyph.plane(), context);

        this.radius = glyph.radius();
        this.opacity = glyph.getOpacity(tickDelta);

        var primaryColor = glyph.primaryColor();
        this.primaryRed = primaryColor.getRed(tickDelta);
        this.primaryGreen = primaryColor.getGreen(tickDelta);
        this.primaryBlue = primaryColor.getBlue(tickDelta);

        var secondaryColor = glyph.secondaryColor();
        this.secondaryRed = secondaryColor.getRed(tickDelta);
        this.secondaryGreen = secondaryColor.getGreen(tickDelta);
        this.secondaryBlue = secondaryColor.getBlue(tickDelta);

        this.shape = glyph.shape();
        this.highlightNodes = glyph.source().matchesEntity(context.camera().getFocusedEntity());
        this.stroke = glyph.getStroke(tickDelta);
    }

    public void setSpell(PreparedGlyph glyph, WorldRenderContext context) {
        this.setTransform(glyph.source(), glyph.transform(), context);
        this.setForm(glyph.form());

        this.opacity = 1.0F;
        this.highlightNodes = false;
        this.stroke = null;
    }

    public void setFading(FadingGlyph glyph, WorldRenderContext context) {
        this.setTransform(glyph.source(), glyph.plane(), context);
        this.setForm(glyph.form());

        this.opacity = glyph.getOpacity(context.tickDelta());
        this.highlightNodes = false;
        this.stroke = null;
    }

    private void setTransform(SpellSource source, GlyphTransform transform, WorldRenderContext context) {
        var plane = this.plane;
        plane.set(transform, context.tickDelta());

        this.setTransform(source, plane, context);
    }

    private void setTransform(SpellSource source, GlyphPlane plane, WorldRenderContext context) {
        var modelMatrix = context.matrixStack().peek().getPositionMatrix();

        var cameraPos = context.camera().getPos();
        var sourcePos = source.getPosition(context.tickDelta());

        var modelViewProject = this.modelViewProject;
        modelViewProject.load(context.projectionMatrix());
        modelViewProject.multiply(modelMatrix);
        modelViewProject.multiplyByTranslation(
                (float) (sourcePos.x - cameraPos.x),
                (float) (sourcePos.y - cameraPos.y),
                (float) (sourcePos.z - cameraPos.z)
        );
        modelViewProject.multiply(plane.getTransformationMatrix());

        this.distance = plane.getDistance();
    }

    private void setForm(GlyphForm form) {
        this.radius = form.radius();
        this.shape = form.shape();

        var primaryColor = form.style().primaryColor();
        this.primaryRed = primaryColor.red();
        this.primaryGreen = primaryColor.green();
        this.primaryBlue = primaryColor.blue();

        var secondaryColor = form.style().secondaryColor();
        this.secondaryRed = secondaryColor.red();
        this.secondaryGreen = secondaryColor.green();
        this.secondaryBlue = secondaryColor.blue();
    }

    public interface Applicator<T> {
        void set(GlyphRenderParameters parameters, T glyph, WorldRenderContext context);
    }
}
