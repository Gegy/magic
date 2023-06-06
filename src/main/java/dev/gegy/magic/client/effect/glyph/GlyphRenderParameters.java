package dev.gegy.magic.client.effect.glyph;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.casting.drawing.FadingGlyph;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.SpellCastingGlyph;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.math.AnimatedColor;
import dev.gegy.magic.math.ColorRgb;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public final class GlyphRenderParameters {
    public final Matrix4f modelViewProject = new Matrix4f();
    public float radius;
    public float opacity;
    public float primaryRed, primaryGreen, primaryBlue;
    public float secondaryRed, secondaryGreen, secondaryBlue;
    public GlyphShape shape = GlyphShape.EMPTY;

    public boolean highlightNodes;

    @Nullable
    public GlyphStroke stroke;

    private final GlyphPlane plane = new GlyphPlane();

    public void setDrawing(final ClientDrawingGlyph glyph, final WorldRenderContext context) {
        final float tickDelta = context.tickDelta();

        setTransform(glyph.source(), glyph.plane(), context);

        radius = glyph.radius();
        opacity = glyph.getOpacity(tickDelta);

        final AnimatedColor primaryColor = glyph.primaryColor();
        primaryRed = primaryColor.getRed(tickDelta);
        primaryGreen = primaryColor.getGreen(tickDelta);
        primaryBlue = primaryColor.getBlue(tickDelta);

        final AnimatedColor secondaryColor = glyph.secondaryColor();
        secondaryRed = secondaryColor.getRed(tickDelta);
        secondaryGreen = secondaryColor.getGreen(tickDelta);
        secondaryBlue = secondaryColor.getBlue(tickDelta);

        shape = glyph.shape();
        highlightNodes = glyph.source().matchesEntity(context.camera().getEntity());
        stroke = glyph.getStroke(tickDelta);
    }

    public void setSpell(final SpellCastingGlyph glyph, final WorldRenderContext context) {
        setTransform(glyph.source(), glyph.transform(), context);
        setForm(glyph.form());

        opacity = 1.0f;
        highlightNodes = false;
        stroke = null;
    }

    public void setFading(final FadingGlyph glyph, final WorldRenderContext context) {
        setTransform(glyph.source(), glyph.plane(), context);
        setForm(glyph.form());

        opacity = glyph.getOpacity(context.tickDelta());
        highlightNodes = false;
        stroke = null;
    }

    private void setTransform(final SpellSource source, final GlyphTransform transform, final WorldRenderContext context) {
        final GlyphPlane plane = this.plane;
        plane.set(transform, context.tickDelta());

        setTransform(source, plane, context);
    }

    private void setTransform(final SpellSource source, final GlyphPlane plane, final WorldRenderContext context) {
        final Matrix4f modelMatrix = context.matrixStack().last().pose();

        final Vec3 cameraPos = context.camera().getPosition();
        final Vec3 sourcePos = source.getPosition(context.tickDelta());

        modelViewProject.set(context.projectionMatrix())
                .mul(modelMatrix)
                .translate(
                        (float) (sourcePos.x - cameraPos.x),
                        (float) (sourcePos.y - cameraPos.y),
                        (float) (sourcePos.z - cameraPos.z)
                )
                .mul(plane.planeToWorld());
    }

    private void setForm(final GlyphForm form) {
        radius = form.radius();
        shape = form.shape();

        final ColorRgb primaryColor = form.style().primaryColor();
        primaryRed = primaryColor.red();
        primaryGreen = primaryColor.green();
        primaryBlue = primaryColor.blue();

        final ColorRgb secondaryColor = form.style().secondaryColor();
        secondaryRed = secondaryColor.red();
        secondaryGreen = secondaryColor.green();
        secondaryBlue = secondaryColor.blue();
    }

    public interface Applicator<T> {
        void set(GlyphRenderParameters parameters, T glyph, WorldRenderContext context);
    }
}
