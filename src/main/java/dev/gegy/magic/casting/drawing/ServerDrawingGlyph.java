package dev.gegy.magic.casting.drawing;

import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.glyph.shape.GlyphShapeStorage;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public final class ServerDrawingGlyph {
    private final Vec3f direction;
    private final float radius;

    private int shape;
    private GlyphType formedType;

    private GlyphNode stroke;

    public ServerDrawingGlyph(Vec3f direction, float radius) {
        this.direction = direction;
        this.radius = radius;
    }

    public Vec3f direction() {
        return this.direction;
    }

    public float radius() {
        return this.radius;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public void setStroke(@Nullable GlyphNode stroke) {
        this.stroke = stroke;
    }

    public boolean tryForm(GlyphShapeStorage glyphShapes) {
        int shape = this.shape;
        var matchingType = glyphShapes.getGlyphForShape(shape);
        if (matchingType != null) {
            this.formedType = matchingType;
            this.stroke = null;
            return true;
        } else {
            return false;
        }
    }

    public DrawingGlyphParameters asParameters() {
        return new DrawingGlyphParameters(this.direction, this.radius, this.shape, this.formedType);
    }

    public int getShape() {
        return this.shape;
    }

    @Nullable
    public GlyphType getFormedType() {
        return this.formedType;
    }

    @Nullable
    public GlyphNode getStroke() {
        return this.stroke;
    }
}
