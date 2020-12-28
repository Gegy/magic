package dev.gegy.magic.glyph;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.*;
import java.util.function.Consumer;

public final class GlyphGenerator {
    private final int minSize;
    private final int maxSize;

    public GlyphGenerator(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public List<Glyph> generateAll() {
        // 1. pick a point
        // 2. pick another point connected to the current point and construct an edge
        // 3. if this new point is on the circumference, we can either:
        //       - continue our current path and return to step 2
        //       - start from a newly picked point
        //    when picking a new point, we CAN pick a point that has already been picked
        //    but, we CANNOT pick an edge that has already been constructed

        Set<Glyph> result = new ObjectOpenHashSet<>();
        this.generateAll(result::add);
        return new ArrayList<>(result);
    }

    private void generateAll(Consumer<Glyph> yield) {
        // we always have to start from a point on the circumference because that is where our pen must start!
        for (GlyphNode rootNode : GlyphNode.CIRCUMFERENCE) {
            this.advanceFromRoot(new GlyphEdge[0], rootNode, yield);
        }
    }

    private void advanceFromRoot(GlyphEdge[] glyph, GlyphNode node, Consumer<Glyph> yield) {
        for (GlyphNode nextNode : node.getConnections()) {
            this.tryAdvanceWithEdge(glyph, node, nextNode, yield);
        }
    }

    private void tryAdvanceFrom(GlyphEdge[] glyph, GlyphNode node, Consumer<Glyph> yield) {
        GlyphEdge[] simplifiedGlyph = simplify(glyph);

        // if this glyph has enough edges, yield
        if (simplifiedGlyph.length >= this.minSize) {
            yield.accept(new Glyph(simplifiedGlyph));
        }

        // only continue picking new points if this glyph has not exceeded the maximum size
        if (simplifiedGlyph.length >= this.maxSize) {
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

    private void tryAdvanceWithEdge(GlyphEdge[] glyph, GlyphNode node, GlyphNode nextNode, Consumer<Glyph> yield) {
        GlyphEdge nextEdge = new GlyphEdge(node, nextNode);

        // don't append this edge if we've already used it
        if (containsEdge(glyph, nextEdge)) {
            return;
        }

        GlyphEdge[] nextGlyph = Arrays.copyOf(glyph, glyph.length + 1);
        nextGlyph[glyph.length] = nextEdge;

        this.tryAdvanceFrom(nextGlyph, nextNode, yield);
    }

    private static GlyphEdge[] simplify(GlyphEdge[] glyph) {
        if (!canSimplify(glyph)) {
            return glyph;
        }

        // all the points along the center are colinear, and to give an accurate measurement of the "size" of a glyph,
        // the merged line across is more meaningful.
        GlyphNode[] centerLine = GlyphNode.CENTER_LINE;

        List<GlyphEdge> simplified = new ArrayList<>(glyph.length);
        Collections.addAll(simplified, glyph);

        GlyphNode from = null;

        for (int i = 0; i < centerLine.length - 1; i++) {
            GlyphEdge edge = new GlyphEdge(centerLine[i], centerLine[i + 1]);
            if (simplified.remove(edge)) {
                if (from == null) {
                    from = centerLine[i];
                }
            } else {
                if (from != null) {
                    simplified.add(new GlyphEdge(from, centerLine[i]));
                    from = null;
                }
            }
        }

        if (from != null) {
            simplified.add(new GlyphEdge(from, centerLine[centerLine.length - 1]));
        }

        return simplified.toArray(new GlyphEdge[0]);
    }

    private static boolean canSimplify(GlyphEdge[] glyph) {
        // we can potentially simplify if we have 2+ edges in the center
        int count = 0;
        for (GlyphEdge edge : glyph) {
            if (isCenterEdge(edge)) {
                if (++count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCenterEdge(GlyphEdge edge) {
        return isCenterNode(edge.from) && isCenterNode(edge.to);
    }

    private static boolean isCenterNode(GlyphNode node) {
        for (GlyphNode centerNode : GlyphNode.CENTER_LINE) {
            if (node == centerNode) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsEdge(GlyphEdge[] glyph, GlyphEdge edge) {
        for (GlyphEdge other : glyph) {
            if (other.equals(edge)) {
                return true;
            }
        }
        return false;
    }
}
