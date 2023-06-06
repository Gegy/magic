package dev.gegy.magic.glyph.shape;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public final class GlyphShapeGenerator {
    private final int minSize;
    private final int maxSize;

    public GlyphShapeGenerator(final int minSize, final int maxSize) {
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

        final Set<GlyphShape> result = new ObjectOpenHashSet<>();
        generateAll(result::add);
        return new ArrayList<>(result);
    }

    private void generateAll(final Consumer<GlyphShape> add) {
        // we always have to start from a point on the circumference because that is where our pen must start!
        for (final GlyphNode rootNode : GlyphNode.CIRCUMFERENCE) {
            advanceFromRoot(GlyphShape.EMPTY, rootNode, add);
        }
    }

    private void advanceFromRoot(final GlyphShape glyph, final GlyphNode node, final Consumer<GlyphShape> add) {
        for (final GlyphEdge nextEdge : GlyphEdge.getConnectedEdgesTo(node)) {
            tryAdvanceWithEdge(glyph, node, nextEdge, add);
        }
    }

    private void tryAdvanceFrom(final GlyphShape glyph, final GlyphNode node, final Consumer<GlyphShape> add) {
        final int size = glyph.size();

        // if this glyph has enough edges, yield
        if (size >= minSize) {
            add.accept(glyph);
        }

        // only continue picking new points if this glyph has not exceeded the maximum size
        if (size >= maxSize) {
            return;
        }

        // if this node is on the circumference, we can "pick up our pencil" to jump to a new node on the circumference
        if (node.isAtCircumference()) {
            for (final GlyphNode nextNode : GlyphNode.CIRCUMFERENCE) {
                if (nextNode != node) {
                    advanceFromRoot(glyph, nextNode, add);
                }
            }
        }

        // find the all next nodes to travel to
        for (final GlyphEdge nextEdge : GlyphEdge.getConnectedEdgesTo(node)) {
            tryAdvanceWithEdge(glyph, node, nextEdge, add);
        }
    }

    private void tryAdvanceWithEdge(final GlyphShape glyph, final GlyphNode node, final GlyphEdge nextEdge, final Consumer<GlyphShape> add) {
        if (!glyph.contains(nextEdge)) {
            final GlyphShape nextGlyph = glyph.withEdge(nextEdge);
            final GlyphNode nextNode = nextEdge.getOther(node);
            tryAdvanceFrom(nextGlyph, nextNode, add);
        }
    }
}
