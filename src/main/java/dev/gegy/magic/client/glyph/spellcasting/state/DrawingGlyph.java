package dev.gegy.magic.client.glyph.spellcasting.state;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.GlyphStroke;
import dev.gegy.magic.client.glyph.plane.GlyphPlane;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.c2s.CancelGlyphC2SPacket;
import dev.gegy.magic.network.c2s.DrawGlyphC2SPacket;
import dev.gegy.magic.spell.Spell;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public abstract class DrawingGlyph implements SpellcastingState {
    // 15% of circle radius
    private static final float SELECT_DISTANCE = 0.15F;
    private static final float SELECT_DISTANCE_2 = SELECT_DISTANCE * SELECT_DISTANCE;

    private static final float DRAWING_RADIUS = 1.0F + SELECT_DISTANCE;
    private static final float DRAWING_RADIUS_2 = DRAWING_RADIUS * DRAWING_RADIUS;

    protected final ClientGlyph glyph;
    protected final GlyphPlane plane;

    private final Vector3f sample = new Vector3f();

    private Vec3d lastLook;

    DrawingGlyph(ClientGlyph glyph, GlyphPlane plane) {
        this.glyph = glyph;
        this.plane = plane;
    }

    @Override
    public final SpellcastingState tick(ClientPlayerEntity player) {
        if (player.isSneaking()) {
            CancelGlyphC2SPacket.sendToServer();
            return new ContinueSpellcasting();
        }

        this.glyph.tick();

        Vec3d look = player.getRotationVec(1.0F);
        if (look.equals(this.lastLook)) {
            return this;
        }

        this.lastLook = look;

        ClientGlyph glyph = this.glyph;

        Vector3f sample = this.sample;
        sample.set((float) look.x, (float) look.y, (float) look.z);
        this.plane.projectOntoPlane(sample);

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

    @Override
    public final SpellcastingState finishDrawingGlyph(Spell spell, Consumer<ClientGlyph> yield) {
        this.glyph.applySpell(spell);
        yield.accept(this.glyph);

        return new ContinueSpellcasting();
    }

    protected abstract SpellcastingState tickDraw(float x, float y);

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

    static final class OutsideCircle extends DrawingGlyph {
        OutsideCircle(ClientGlyph glyph, GlyphPlane plane) {
            super(glyph, plane);
        }

        @Override
        protected SpellcastingState tickDraw(float x, float y) {
            GlyphNode node = this.selectNodeAt(GlyphNode.CIRCUMFERENCE, x, y);
            if (node != null) {
                return new DrawingLine(this.glyph, this.plane, node);
            }

            return this;
        }
    }

    static final class DrawingLine extends DrawingGlyph {
        private final GlyphNode fromNode;
        private final GlyphNode[] connectedNodes;

        private final GlyphStroke stroke;

        DrawingLine(ClientGlyph glyph, GlyphPlane plane, GlyphNode fromNode) {
            super(glyph, plane);
            this.fromNode = fromNode;
            this.connectedNodes = GlyphEdge.getConnectedNodesTo(fromNode);

            this.stroke = glyph.startStroke(fromNode.getPoint());
        }

        @Override
        protected SpellcastingState tickDraw(float x, float y) {
            if (this.isOutsideCircle(x, y)) {
                this.glyph.stopStroke();
                return new OutsideCircle(this.glyph, this.plane);
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

        private DrawingGlyph selectNode(GlyphNode toNode) {
            GlyphEdge edge = GlyphEdge.between(this.fromNode, toNode);
            if (edge == null) {
                throw new IllegalStateException("missing edge between " + this.fromNode + " to " + toNode);
            }

            this.putEdge(edge);

            if (toNode.isAtCircumference()) {
                return new OutsideCircle(this.glyph, this.plane);
            } else {
                return new DrawingLine(this.glyph, this.plane, toNode);
            }
        }
    }
}
