package dev.gegy.magic.casting.drawing;

import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.glyph.shape.GlyphShapeStorage;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class ServerDrawingGlyph {
    private final Vector3f direction;
    private final float radius;

    private int shape;
    private GlyphType formedType;

    private GlyphNode stroke;

    public ServerDrawingGlyph(Vector3f direction, float radius) {
        this.direction = direction;
        this.radius = radius;
    }

    public Vector3f direction() {
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

    @Nullable
    public GlyphForm asForm() {
        var formedType = this.formedType;
        if (formedType != null) {
            return new GlyphForm(this.radius, this.shape, formedType.style());
        } else {
            return null;
        }
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
