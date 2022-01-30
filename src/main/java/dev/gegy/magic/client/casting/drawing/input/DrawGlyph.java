package dev.gegy.magic.client.casting.drawing.input;

import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

abstract class DrawGlyph implements DrawingInputState {
    // 15% of circle radius
    private static final float SELECT_DISTANCE = 0.15F;
    private static final float SELECT_DISTANCE_2 = SELECT_DISTANCE * SELECT_DISTANCE;

    private static final float DRAWING_RADIUS = 1.0F + SELECT_DISTANCE;
    private static final float DRAWING_RADIUS_2 = DRAWING_RADIUS * DRAWING_RADIUS;

    protected final ClientDrawingGlyph glyph;
    protected final GlyphPlane plane;

    DrawGlyph(ClientDrawingGlyph glyph, GlyphPlane plane) {
        this.glyph = glyph;
        this.plane = plane;
    }

    @Override
    public final DrawingInputState tick(ClientCastingDrawing casting, PlayerEntity player) {
        this.glyph.tick();

        Vec3f drawPointer = this.glyph.drawPointer();

        if (this.isPointerOutOfBounds(drawPointer)) {
            return this.cancelGlyph(casting);
        }

        float radius = this.glyph.radius();
        return this.tickDraw(casting,
                Math.abs(drawPointer.getX() / radius),
                drawPointer.getY() / radius
        );
    }

    private ContinueDraw cancelGlyph(ClientCastingDrawing casting) {
        casting.senders().cancelGlyph();
        return new ContinueDraw();
    }

    private boolean isPointerOutOfBounds(Vec3f drawPointer) {
        float radius = this.glyph.radius();
        float x = drawPointer.getX() / radius;
        float y = drawPointer.getY() / radius;
        float distance2 = x * x + y * y;

        return distance2 >= 3.0F * 3.0F;
    }

    @Override
    @Nullable
    public final ClientDrawingGlyph getDrawingGlyph() {
        return this.glyph;
    }

    @Override
    public final DrawingInputState finishDrawingGlyph(GlyphType matchedType) {
        this.glyph.applyFormedType(matchedType);
        return new ContinueDraw();
    }

    @Override
    public final DrawingInputState cancelDrawingGlyph() {
        return new ContinueDraw();
    }

    protected abstract DrawingInputState tickDraw(ClientCastingDrawing casting, float x, float y);

    protected boolean putEdge(ClientCastingDrawing casting, GlyphEdge edge) {
        if (this.glyph.putEdge(edge)) {
            casting.senders().drawGlyphShape(this.glyph.shape());
            return true;
        }
        return false;
    }

    protected void startStroke(ClientCastingDrawing casting, GlyphNode node) {
        this.glyph.startStroke(node);
        casting.senders().startGlyphStroke(node);
    }

    protected void stopStroke(ClientCastingDrawing casting) {
        this.glyph.stopStroke();
        casting.senders().stopGlyphStroke();
    }

    protected boolean isOutsideCircle(float x, float y) {
        return x * x + y * y > DRAWING_RADIUS_2;
    }

    @Nullable
    protected GlyphNode selectNodeAt(GlyphNode[] nodes, float x, float y) {
        for (GlyphNode node : nodes) {
            Vec2f point = node.getPoint();
            float deltaX = point.x - x;
            float deltaY = point.y - y;
            if (deltaX * deltaX + deltaY * deltaY < SELECT_DISTANCE_2) {
                return node;
            }
        }
        return null;
    }

    static final class OutsideCircle extends DrawGlyph {
        OutsideCircle(ClientDrawingGlyph glyph, GlyphPlane plane) {
            super(glyph, plane);
        }

        @Override
        protected DrawingInputState tickDraw(ClientCastingDrawing casting, float x, float y) {
            GlyphNode node = this.selectNodeAt(GlyphNode.CIRCUMFERENCE, x, y);
            if (node != null) {
                this.startStroke(casting, node);
                return new Line(this.glyph, this.plane, node);
            }

            return this;
        }
    }

    static final class Line extends DrawGlyph {
        private final GlyphNode fromNode;
        private final GlyphNode[] connectedNodes;

        Line(ClientDrawingGlyph glyph, GlyphPlane plane, GlyphNode fromNode) {
            super(glyph, plane);
            this.fromNode = fromNode;
            this.connectedNodes = GlyphEdge.getConnectedNodesTo(fromNode);
        }

        @Override
        protected DrawingInputState tickDraw(ClientCastingDrawing casting, float x, float y) {
            if (this.isOutsideCircle(x, y)) {
                this.stopStroke(casting);
                return new OutsideCircle(this.glyph, this.plane);
            }

            GlyphNode toNode = this.selectNodeAt(this.connectedNodes, x, y);
            if (toNode != null) {
                this.stopStroke(casting);
                return this.selectNode(casting, toNode);
            } else {
                return this;
            }
        }

        private DrawGlyph selectNode(ClientCastingDrawing casting, GlyphNode toNode) {
            GlyphEdge edge = GlyphEdge.between(this.fromNode, toNode);
            if (edge == null) {
                throw new IllegalStateException("missing edge between " + this.fromNode + " to " + toNode);
            }

            this.putEdge(casting, edge);

            if (toNode.isAtCircumference()) {
                return new OutsideCircle(this.glyph, this.plane);
            } else {
                this.startStroke(casting, toNode);
                return new Line(this.glyph, this.plane, toNode);
            }
        }
    }
}
