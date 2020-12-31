package dev.gegy.magic.glyph.shape;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;
import java.util.function.Consumer;

public final class GlyphShapeGenerator {
    private final int minSize;
    private final int maxSize;

    public GlyphShapeGenerator(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public List<GlyphShape> generateAll() {
        // 1. pick a point
        // 2. pick another point connected to the current point and construct an edge
        // 3. if this new point is on the circumference, we can either:
        //       - continue our current path and return to step 2
        //       - start from a newly picked point
        //    when picking a new point, we CAN pick a point that has already been picked
        //    but, we CANNOT pick an edge that has already been constructed

        Set<GlyphShape> result = new ObjectOpenHashSet<>();
        this.generateAll(result::add);
        return new ArrayList<>(result);
    }

    private void generateAll(Consumer<GlyphShape> yield) {
        // we always have to start from a point on the circumference because that is where our pen must start!
        for (GlyphNode rootNode : GlyphNode.CIRCUMFERENCE) {
            this.advanceFromRoot(new GlyphEdge[0], rootNode, yield);
        }
    }

    private void advanceFromRoot(GlyphEdge[] glyph, GlyphNode node, Consumer<GlyphShape> yield) {
        for (GlyphNode nextNode : node.getConnections()) {
            this.tryAdvanceWithEdge(glyph, node, nextNode, yield);
        }
    }

    private void tryAdvanceFrom(GlyphEdge[] glyph, GlyphNode node, Consumer<GlyphShape> yield) {
        int size = GlyphShape.getSize(glyph);

        // if this glyph has enough edges, yield
        if (size >= this.minSize) {
            yield.accept(new GlyphShape(glyph, size));
        }

        // only continue picking new points if this glyph has not exceeded the maximum size
        if (size >= this.maxSize) {
            return;
        }

        // if this node is on the circumference, we can "pick up our pencil" to jump to a new node on the circumference
        if (node.isAtCircumference()) {
            for (GlyphNode nextNode : GlyphNode.CIRCUMFERENCE) {
                if (nextNode != node) {
                    this.advanceFromRoot(glyph, nextNode, yield);
                }
            }
        }

        // find the all next nodes to travel to
        for (GlyphNode nextNode : node.getConnections()) {
            this.tryAdvanceWithEdge(glyph, node, nextNode, yield);
        }
    }

    private void tryAdvanceWithEdge(GlyphEdge[] glyph, GlyphNode node, GlyphNode nextNode, Consumer<GlyphShape> yield) {
        GlyphEdge nextEdge = new GlyphEdge(node, nextNode);

        // don't append this edge if we've already used it
        if (GlyphShape.containsEdge(glyph, nextEdge)) {
            return;
        }

        GlyphEdge[] nextGlyph = Arrays.copyOf(glyph, glyph.length + 1);
        nextGlyph[glyph.length] = nextEdge;

        this.tryAdvanceFrom(nextGlyph, nextNode, yield);
    }
}
