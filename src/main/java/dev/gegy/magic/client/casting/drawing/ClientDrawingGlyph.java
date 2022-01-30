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
import dev.gegy.magic.math.Easings;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public final class ClientDrawingGlyph {
    public static final int FORM_TICKS = 5;
    private static final float COLOR_LERP_SPEED = 0.15F;

    private final SpellSource source;
    private final GlyphPlane plane;

    private final float radius;

    private final AnimatedColor primaryColor = new AnimatedColor(GlyphStyle.UNDIFFERENTIATED.primaryColor());
    private final AnimatedColor secondaryColor = new AnimatedColor(GlyphStyle.UNDIFFERENTIATED.secondaryColor());

    private int shape;

    private GlyphStrokeTracker stroke;

    private final Vec3f drawPointer = new Vec3f();

    private Vec3d lastLook;

    private int formTicks;

    public ClientDrawingGlyph(SpellSource source, GlyphPlane plane, float radius) {
        this.source = source;
        this.plane = plane;
        this.radius = radius;
    }

    public void tick() {
        if (this.formTicks < FORM_TICKS) {
            this.formTicks++;
        }

        this.primaryColor.tick(COLOR_LERP_SPEED);
        this.secondaryColor.tick(COLOR_LERP_SPEED);

        Vec3f drawPointer = this.computeDrawPointer(this.drawPointer);

        GlyphStrokeTracker stroke = this.stroke;
        if (stroke != null) {
            this.tickStroke(drawPointer, stroke);
        }
    }

    private void tickStroke(Vec3f pointer, GlyphStrokeTracker stroke) {
        float radius = this.radius;
        float x = Math.abs(pointer.getX() / radius);
        float y = pointer.getY() / radius;

        float distance2 = x * x + y * y;
        if (distance2 >= 1.0F) {
            float factor = MathHelper.fastInverseSqrt(distance2);
            x *= factor;
            y *= factor;
        }

        stroke.tick(x, y);
    }

    private Vec3f computeDrawPointer(Vec3f drawPointer) {
        Vec3d look = this.source.getLookVector(1.0F);
        if (look.equals(this.lastLook)) {
            return drawPointer;
        }

        this.lastLook = look;

        drawPointer.set((float) look.x, (float) look.y, (float) look.z);
        this.plane.projectOntoPlane(drawPointer);

        return drawPointer;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public boolean putEdge(GlyphEdge edge) {
        int newShape = this.shape | edge.asBit();
        if (this.shape != newShape) {
            this.shape = newShape;
            return true;
        } else {
            return false;
        }
    }

    public float getOpacity(float tickDelta) {
        float formTicks = Math.min(this.formTicks + tickDelta, FORM_TICKS);
        return Easings.easeInCirc(formTicks / FORM_TICKS);
    }

    public void startStroke(GlyphNode node) {
        Vec2f point = node.getPoint();
        this.stroke = new GlyphStrokeTracker(point.x, point.y);
    }

    public void stopStroke() {
        this.stroke = null;
    }

    public void applyFormedType(GlyphType type) {
        this.primaryColor.set(type.style().primaryColor());
        this.secondaryColor.set(type.style().secondaryColor());
        this.stroke = null;
    }

    public void applyStroke(@Nullable GlyphNode node) {
        if (node != null) {
            this.startStroke(node);
        } else {
            this.stopStroke();
        }
    }

    public SpellSource source() {
        return this.source;
    }

    public GlyphPlane plane() {
        return this.plane;
    }

    public float radius() {
        return this.radius;
    }

    public int shape() {
        return this.shape;
    }

    public AnimatedColor primaryColor() {
        return this.primaryColor;
    }

    public AnimatedColor secondaryColor() {
        return this.secondaryColor;
    }

    public GlyphForm asForm() {
        var style = new GlyphStyle(this.primaryColor.target(), this.secondaryColor.target());
        return new GlyphForm(this.radius, this.shape, style);
    }

    public Vec3f drawPointer() {
        return this.drawPointer;
    }

    @Nullable
    public GlyphStroke getStroke(float tickDelta) {
        return this.stroke != null ? this.stroke.resolve(tickDelta) : null;
    }
}
