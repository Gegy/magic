package dev.gegy.magic.client.glyph.draw;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.c2s.DrawGlyphC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class DrawGlyphEdges implements GlyphDrawState {
    // 15% of circle radius
    private static final float SELECT_DISTANCE = 0.15F;
    private static final float SELECT_DISTANCE_2 = SELECT_DISTANCE * SELECT_DISTANCE;

    private static final float DRAWING_RADIUS = 1.0F + SELECT_DISTANCE;
    private static final float DRAWING_RADIUS_2 = DRAWING_RADIUS * DRAWING_RADIUS;

    protected final ClientGlyph glyph;

    private final Vector3f sample = new Vector3f();

    private Vec3d lastLook;

    DrawGlyphEdges(ClientGlyph glyph) {
        this.glyph = glyph;
    }

    @Override
    public final GlyphDrawState tick(ClientPlayerEntity player) {
        if (player.isSneaking()) {
            return new DrawGlyphOutline();
        }

        Vec3d look = player.getRotationVec(1.0F);
        if (look.equals(this.lastLook)) {
            return this;
        }

        this.lastLook = look;

        ClientGlyph glyph = this.glyph;

        Vector3f sample = this.sample;
        sample.set((float) look.x, (float) look.y, (float) look.z);
        glyph.plane.projectOntoPlane(sample);

        return this.tickDraw(
                Math.abs(sample.getX() / glyph.radius),
                sample.getY() / glyph.radius
        );
    }

    @Override
    @Nullable
    public final ClientGlyph getDrawingGlyph() {
        return this.glyph;
    }

    protected abstract GlyphDrawState tickDraw(float x, float y);

    protected boolean putEdge(GlyphEdge edge) {
        if (this.glyph.putEdge(edge)) {
            DrawGlyphC2SPacket.sendToServer(this.glyph.shape);
            return true;
        }
        return false;
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

    static final class OutsideCircle extends DrawGlyphEdges {
        OutsideCircle(ClientGlyph glyph) {
            super(glyph);
        }

        @Override
        protected GlyphDrawState tickDraw(float x, float y) {
            GlyphNode node = this.selectNodeAt(GlyphNode.CIRCUMFERENCE, x, y);
            if (node != null) {
                return new DrawingLine(this.glyph, node);
            }

            return this;
        }
    }

    static final class DrawingLine extends DrawGlyphEdges {
        private final GlyphNode fromNode;
        private final GlyphNode[] connectedNodes;

        private final GlyphStroke stroke;

        DrawingLine(ClientGlyph glyph, GlyphNode fromNode) {
            super(glyph);
            this.fromNode = fromNode;
            this.connectedNodes = GlyphEdge.getConnectedNodesTo(fromNode);

            this.stroke = glyph.startStroke(fromNode.getPoint());
        }

        @Override
        protected GlyphDrawState tickDraw(float x, float y) {
            if (this.isOutsideCircle(x, y)) {
                this.glyph.stopStroke();
                return new OutsideCircle(this.glyph);
            }

            float radius2 = x * x + y * y;
            if (radius2 >= 1.0F) {
                float radius = (float) Math.sqrt(radius2);
                x /= radius;
                y /= radius;
            }

            this.stroke.update(x, y);

            GlyphNode toNode = this.selectNodeAt(this.connectedNodes, x, y);
            if (toNode != null) {
                this.glyph.stopStroke();
                return this.selectNode(toNode);
            } else {
                return this;
            }
        }

        private DrawGlyphEdges selectNode(GlyphNode toNode) {
            GlyphEdge edge = GlyphEdge.between(this.fromNode, toNode);
            if (edge == null) {
                throw new IllegalStateException("missing edge between " + this.fromNode + " to " + toNode);
            }

            if (!this.putEdge(edge) || toNode.isAtCircumference()) {
                return new OutsideCircle(this.glyph);
            } else {
                return new DrawingLine(this.glyph, toNode);
            }
        }
    }
}
