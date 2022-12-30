package dev.gegy.magic.client.casting.drawing.input;

import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

abstract class DrawGlyph implements DrawingInputState {
    // 15% of circle radius
    private static final float SELECT_DISTANCE = 0.15f;
    private static final float SELECT_DISTANCE_2 = SELECT_DISTANCE * SELECT_DISTANCE;

    private static final float DRAWING_RADIUS = 1.0f + SELECT_DISTANCE;
    private static final float DRAWING_RADIUS_2 = DRAWING_RADIUS * DRAWING_RADIUS;

    protected final ClientDrawingGlyph glyph;
    protected final GlyphPlane plane;

    DrawGlyph(final ClientDrawingGlyph glyph, final GlyphPlane plane) {
        this.glyph = glyph;
        this.plane = plane;
    }

    @Override
    public final DrawingInputState tick(final ClientCastingDrawing casting, final Player player) {
        final Vector3f drawPointer = glyph.drawPointer();
        if (drawPointer == null || isPointerOutOfBounds(drawPointer)) {
            return cancelGlyph(casting);
        }

        final float radius = glyph.radius();
        return tickDraw(casting,
                Math.abs(drawPointer.x() / radius),
                drawPointer.y() / radius
        );
    }

    private ContinueDraw cancelGlyph(final ClientCastingDrawing casting) {
        casting.senders().cancelGlyph();
        return new ContinueDraw();
    }

    private boolean isPointerOutOfBounds(final Vector3f drawPointer) {
        final float radius = glyph.radius();
        final float x = drawPointer.x() / radius;
        final float y = drawPointer.y() / radius;
        final float distance2 = x * x + y * y;

        return distance2 >= 3.0f * 3.0f;
    }

    @Override
    @Nullable
    public final ClientDrawingGlyph getDrawingGlyph() {
        return glyph;
    }

    @Override
    public final DrawingInputState finishDrawingGlyph(final GlyphType matchedType) {
        glyph.applyFormedType(matchedType);
        return new ContinueDraw();
    }

    @Override
    public final DrawingInputState cancelDrawingGlyph() {
        return new ContinueDraw();
    }

    protected abstract DrawingInputState tickDraw(ClientCastingDrawing casting, float x, float y);

    protected boolean putEdge(final ClientCastingDrawing casting, final GlyphEdge edge) {
        if (glyph.putEdge(edge)) {
            casting.senders().drawGlyphShape(glyph.shape());
            return true;
        }
        return false;
    }

    protected void startStroke(final ClientCastingDrawing casting, final GlyphNode node) {
        glyph.startStroke(node);
        casting.senders().startGlyphStroke(node);
    }

    protected void stopStroke(final ClientCastingDrawing casting) {
        glyph.stopStroke();
        casting.senders().stopGlyphStroke();
    }

    protected boolean isOutsideCircle(final float x, final float y) {
        return x * x + y * y > DRAWING_RADIUS_2;
    }

    @Nullable
    protected GlyphNode selectNodeAt(final GlyphNode[] nodes, final float x, final float y) {
        for (final GlyphNode node : nodes) {
            final Vec2 point = node.getPoint();
            final float deltaX = point.x - x;
            final float deltaY = point.y - y;
            if (deltaX * deltaX + deltaY * deltaY < SELECT_DISTANCE_2) {
                return node;
            }
        }
        return null;
    }

    static final class OutsideCircle extends DrawGlyph {
        OutsideCircle(final ClientDrawingGlyph glyph, final GlyphPlane plane) {
            super(glyph, plane);
        }

        @Override
        protected DrawingInputState tickDraw(final ClientCastingDrawing casting, final float x, final float y) {
            final GlyphNode node = selectNodeAt(GlyphNode.CIRCUMFERENCE, x, y);
            if (node != null) {
                startStroke(casting, node);
                return new Line(glyph, plane, node);
            }

            return this;
        }
    }

    static final class Line extends DrawGlyph {
        private final GlyphNode fromNode;
        private final GlyphNode[] connectedNodes;

        Line(final ClientDrawingGlyph glyph, final GlyphPlane plane, final GlyphNode fromNode) {
            super(glyph, plane);
            this.fromNode = fromNode;
            connectedNodes = GlyphEdge.getConnectedNodesTo(fromNode);
        }

        @Override
        protected DrawingInputState tickDraw(final ClientCastingDrawing casting, final float x, final float y) {
            if (isOutsideCircle(x, y)) {
                stopStroke(casting);
                return new OutsideCircle(glyph, plane);
            }

            final GlyphNode toNode = selectNodeAt(connectedNodes, x, y);
            if (toNode != null) {
                stopStroke(casting);
                return selectNode(casting, toNode);
            } else {
                return this;
            }
        }

        private DrawGlyph selectNode(final ClientCastingDrawing casting, final GlyphNode toNode) {
            final GlyphEdge edge = GlyphEdge.between(fromNode, toNode);
            if (edge == null) {
                throw new IllegalStateException("missing edge between " + fromNode + " to " + toNode);
            }

            putEdge(casting, edge);

            if (toNode.isAtCircumference()) {
                return new OutsideCircle(glyph, plane);
            } else {
                startStroke(casting, toNode);
                return new Line(glyph, plane, toNode);
            }
        }
    }
}
