package dev.gegy.magic.client.casting.drawing;

import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.glyph.GlyphStyle;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.math.AnimatedColor;
import dev.gegy.magic.math.AnimationTimer;
import dev.gegy.magic.math.Easings;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class ClientDrawingGlyph {
    public static final int FORM_TICKS = 5;
    private static final float COLOR_LERP_SPEED = 0.15f;

    private final SpellSource source;
    private final GlyphPlane plane;

    private final float radius;

    private final AnimatedColor primaryColor = new AnimatedColor(GlyphStyle.WILD.primaryColor());
    private final AnimatedColor secondaryColor = new AnimatedColor(GlyphStyle.WILD.secondaryColor());

    private int shape;

    private GlyphStrokeTracker stroke;

    private Vector3f drawPointer;
    private Vec3 lastLook;

    private int formTicks;

    public ClientDrawingGlyph(final SpellSource source, final GlyphPlane plane, final float radius) {
        this.source = source;
        this.plane = plane;
        this.radius = radius;
    }

    public void tick() {
        if (formTicks < FORM_TICKS) {
            formTicks++;
        }

        primaryColor.tick(COLOR_LERP_SPEED);
        secondaryColor.tick(COLOR_LERP_SPEED);

        final Vec3 look = source.getLookVector(1.0f);
        if (!look.equals(lastLook)) {
            drawPointer = computeDrawPointer(look);
            lastLook = look;
        }

        final GlyphStrokeTracker stroke = this.stroke;
        if (stroke != null) {
            final Vector3f pointer = drawPointer;
            if (pointer != null) {
                tickStroke(pointer, stroke);
            } else {
                this.stroke = null;
            }
        }
    }

    private void tickStroke(final Vector3f pointer, final GlyphStrokeTracker stroke) {
        final float radius = this.radius;
        float x = Math.abs(pointer.x() / radius);
        float y = pointer.y() / radius;

        final float distance2 = x * x + y * y;
        if (distance2 >= 1.0f) {
            final float factor = Mth.invSqrt(distance2);
            x *= factor;
            y *= factor;
        }

        stroke.tick(x, y);
    }

    @Nullable
    private Vector3f computeDrawPointer(final Vec3 look) {
        final Vector3f intersection = plane.raycast(new Vector3f(0.0f, 0.0f, 0.0f), look.toVector3f());
        if (intersection != null) {
            plane.projectFromWorld(intersection);
            return intersection;
        } else {
            return null;
        }
    }

    public void setShape(final int shape) {
        this.shape = shape;
    }

    public boolean putEdge(final GlyphEdge edge) {
        final int newShape = shape | edge.asBit();
        if (shape != newShape) {
            shape = newShape;
            return true;
        } else {
            return false;
        }
    }

    public float getOpacity(final float tickDelta) {
        final float formTicks = Math.min(this.formTicks + tickDelta, FORM_TICKS);
        return Easings.easeInCirc(formTicks / FORM_TICKS);
    }

    public void startStroke(final GlyphNode node) {
        final Vec2 point = node.getPoint();
        stroke = new GlyphStrokeTracker(point.x, point.y);
    }

    public void stopStroke() {
        stroke = null;
    }

    public void applyFormedType(final GlyphType type) {
        primaryColor.set(type.style().primaryColor());
        secondaryColor.set(type.style().secondaryColor());
        stroke = null;
    }

    public void applyStroke(@Nullable final GlyphNode node) {
        if (node != null) {
            startStroke(node);
        } else {
            stopStroke();
        }
    }

    public SpellSource source() {
        return source;
    }

    public GlyphPlane plane() {
        return plane;
    }

    public float radius() {
        return radius;
    }

    public int shape() {
        return shape;
    }

    public AnimatedColor primaryColor() {
        return primaryColor;
    }

    public AnimatedColor secondaryColor() {
        return secondaryColor;
    }

    public GlyphForm asForm() {
        final GlyphStyle style = new GlyphStyle(primaryColor.target(), secondaryColor.target());
        return new GlyphForm(radius, shape, style);
    }

    @Nullable
    public Vector3f drawPointer() {
        return drawPointer;
    }

    @Nullable
    public GlyphStroke getStroke(final float tickDelta) {
        return stroke != null ? stroke.resolve(tickDelta) : null;
    }

    public FadingGlyph toFading(final AnimationTimer timer) {
        return new FadingGlyph(source, plane, asForm(), timer);
    }
}
