package dev.gegy.magic.casting.drawing;

import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.glyph.shape.GlyphShapeStorage;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class ServerDrawingGlyph {
    private final Vector3f direction;
    private final float radius;

    private GlyphShape shape;
    private GlyphType formedType;

    private GlyphNode stroke;

    public ServerDrawingGlyph(final Vector3f direction, final float radius) {
        this.direction = direction;
        this.radius = radius;
    }

    public Vector3f direction() {
        return direction;
    }

    public float radius() {
        return radius;
    }

    public void setShape(final GlyphShape shape) {
        this.shape = shape;
    }

    public void setStroke(@Nullable final GlyphNode stroke) {
        this.stroke = stroke;
    }

    public boolean tryForm(final GlyphShapeStorage glyphShapes) {
        final GlyphShape shape = this.shape;
        final GlyphType matchingType = glyphShapes.getGlyphForShape(shape);
        if (matchingType != null) {
            formedType = matchingType;
            stroke = null;
            return true;
        } else {
            return false;
        }
    }

    public DrawingGlyphParameters asParameters() {
        return new DrawingGlyphParameters(direction, radius, shape, formedType);
    }

    @Nullable
    public GlyphForm asForm() {
        final GlyphType formedType = this.formedType;
        if (formedType != null) {
            return new GlyphForm(radius, shape, formedType.style());
        } else {
            return null;
        }
    }

    public GlyphShape getShape() {
        return shape;
    }

    @Nullable
    public GlyphType getFormedType() {
        return formedType;
    }

    @Nullable
    public GlyphNode getStroke() {
        return stroke;
    }
}
