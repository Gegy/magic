package dev.gegy.magic.client.spellcasting.state;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.glyph.transform.GlyphPlane;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.c2s.CancelGlyphC2SPacket;
import dev.gegy.magic.network.c2s.DrawGlyphShapeC2SPacket;
import dev.gegy.magic.network.c2s.DrawGlyphStrokeC2SPacket;
import dev.gegy.magic.glyph.GlyphType;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

abstract class DrawGlyph implements SpellCastState {
    // 15% of circle radius
    private static final float SELECT_DISTANCE = 0.15F;
    private static final float SELECT_DISTANCE_2 = SELECT_DISTANCE * SELECT_DISTANCE;

    private static final float DRAWING_RADIUS = 1.0F + SELECT_DISTANCE;
    private static final float DRAWING_RADIUS_2 = DRAWING_RADIUS * DRAWING_RADIUS;

    protected final ClientGlyph glyph;
    protected final GlyphPlane plane;

    DrawGlyph(ClientGlyph glyph, GlyphPlane plane) {
        this.glyph = glyph;
        this.plane = plane;
    }

    @Override
    public final SpellCastState tick(ClientPlayerEntity player) {
        if (player.isSneaking()) {
            CancelGlyphC2SPacket.sendToServer();
            return new ContinueDraw();
        }

        this.glyph.tick();

        Vec3f lookingAt = this.glyph.getLookingAt();
        float radius = this.glyph.radius;
        return this.tickDraw(
                Math.abs(lookingAt.getX() / radius),
                lookingAt.getY() / radius
        );
    }

    @Override
    @Nullable
    public final ClientGlyph getDrawingGlyph() {
        return this.glyph;
    }

    @Override
    public final FinishDrawingResult finishDrawingGlyph(GlyphType matchedType) {
        this.glyph.applyMatchedType(matchedType);
        return FinishDrawingResult.finishGlyph(new ContinueDraw(), this.glyph);
    }

    protected abstract SpellCastState tickDraw(float x, float y);

    protected boolean putEdge(GlyphEdge edge) {
        if (this.glyph.putEdge(edge)) {
            DrawGlyphShapeC2SPacket.sendToServer(this.glyph.shape);
            return true;
        }
        return false;
    }

    protected GlyphStroke startStroke(GlyphNode node) {
        GlyphStroke stroke = this.glyph.startStroke(node);
        DrawGlyphStrokeC2SPacket.sendStartToServer(node);
        return stroke;
    }

    protected void stopStroke() {
        this.glyph.stopStroke();
        DrawGlyphStrokeC2SPacket.sendStopToServer();
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
        OutsideCircle(ClientGlyph glyph, GlyphPlane plane) {
            super(glyph, plane);
        }

        @Override
        protected SpellCastState tickDraw(float x, float y) {
            GlyphNode node = this.selectNodeAt(GlyphNode.CIRCUMFERENCE, x, y);
            if (node != null) {
                return new Line(this.glyph, this.plane, node);
            }

            return this;
        }
    }

    static final class Line extends DrawGlyph {
        private final GlyphNode fromNode;
        private final GlyphNode[] connectedNodes;

        Line(ClientGlyph glyph, GlyphPlane plane, GlyphNode fromNode) {
            super(glyph, plane);
            this.fromNode = fromNode;
            this.connectedNodes = GlyphEdge.getConnectedNodesTo(fromNode);

            this.startStroke(fromNode);
        }

        @Override
        protected SpellCastState tickDraw(float x, float y) {
            if (this.isOutsideCircle(x, y)) {
                this.stopStroke();
                return new OutsideCircle(this.glyph, this.plane);
            }

            GlyphNode toNode = this.selectNodeAt(this.connectedNodes, x, y);
            if (toNode != null) {
                this.stopStroke();
                return this.selectNode(toNode);
            } else {
                return this;
            }
        }

        private DrawGlyph selectNode(GlyphNode toNode) {
            GlyphEdge edge = GlyphEdge.between(this.fromNode, toNode);
            if (edge == null) {
                throw new IllegalStateException("missing edge between " + this.fromNode + " to " + toNode);
            }

            this.putEdge(edge);

            if (toNode.isAtCircumference()) {
                return new OutsideCircle(this.glyph, this.plane);
            } else {
                return new Line(this.glyph, this.plane, toNode);
            }
        }
    }
}