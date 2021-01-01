package dev.gegy.magic.client.glyph.draw;

import dev.gegy.magic.glyph.Glyph;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public abstract class DrawingGlyphDrawState implements GlyphDrawState {
    // 10% of circle radius
    private static final float SELECT_DISTANCE = 0.2F;
    private static final float SELECT_DISTANCE_2 = SELECT_DISTANCE * SELECT_DISTANCE;

    private static final float DRAWING_RADIUS = 1.0F + SELECT_DISTANCE;
    private static final float DRAWING_RADIUS_2 = DRAWING_RADIUS * DRAWING_RADIUS;

    protected final Glyph glyph;

    DrawingGlyphDrawState(Glyph glyph) {
        this.glyph = glyph;
    }

    @Override
    public GlyphDrawState tick(ClientPlayerEntity player) {
        if (player.isSneaking()) {
            return new IdleGlyphDrawState();
        }

        Glyph glyph = this.glyph;

        Vec3d look = player.getRotationVec(1.0F);
        Vector3f sample = new Vector3f((float) look.x, (float) look.y, (float) look.z);
        sample.transform(glyph.worldToGlyph);

        return this.tickDraw(
                Math.abs((sample.getX() - glyph.centerX) / glyph.radius),
                (sample.getY() - glyph.centerY) / glyph.radius
        );
    }

    protected abstract GlyphDrawState tickDraw(float x, float y);

    protected boolean isOutsideCircle(float x, float y) {
        return x * x + y * y > DRAWING_RADIUS_2;
    }

    protected void putEdge(GlyphEdge edge) {
        this.glyph.putEdge(edge);
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

    static final class OutsideCircle extends DrawingGlyphDrawState {
        OutsideCircle(Glyph glyph) {
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

    static final class DrawingLine extends DrawingGlyphDrawState {
        private final GlyphNode fromNode;
        private final GlyphNode[] connectedNodes;

        DrawingLine(Glyph glyph, GlyphNode fromNode) {
            super(glyph);
            this.fromNode = fromNode;
            this.connectedNodes = GlyphEdge.getConnectedNodesTo(fromNode);
        }

        @Override
        protected GlyphDrawState tickDraw(float x, float y) {
            if (this.isOutsideCircle(x, y)) {
                return new OutsideCircle(this.glyph);
            }

            GlyphNode toNode = this.selectNodeAt(this.connectedNodes, x, y);
            if (toNode != null) {
                return this.selectNode(toNode);
            } else {
                return this;
            }
        }

        private DrawingGlyphDrawState selectNode(GlyphNode toNode) {
            GlyphEdge edge = GlyphEdge.between(this.fromNode, toNode);
            if (edge == null) {
                throw new IllegalStateException("missing edge between " + this.fromNode + " to " + toNode);
            }

            this.putEdge(edge);

            if (toNode.isAtCircumference()) {
                return new OutsideCircle(this.glyph);
            } else {
                return new DrawingLine(this.glyph, toNode);
            }
        }
    }
}
