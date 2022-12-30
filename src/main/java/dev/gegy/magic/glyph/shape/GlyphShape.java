package dev.gegy.magic.glyph.shape;

import java.util.ArrayList;
import java.util.List;

public final class GlyphShape {
    public static final GlyphShape EMPTY = new GlyphShape(new GlyphEdge[0]);

    public final GlyphEdge[] edges;
    public final int size;
    private final int bits;

    public GlyphShape(final GlyphEdge[] edges, final int size) {
        this.edges = edges;
        this.size = size;
        bits = edgesToBits(edges);
    }

    public GlyphShape(final int bits) {
        edges = bitsToEdges(bits);
        size = getSize(edges);
        this.bits = bits;
    }

    public GlyphShape(final GlyphEdge[] edges) {
        this(edges, getSize(edges));
    }

    static int getSize(final GlyphEdge[] glyph) {
        if (!canSimplify(glyph)) {
            return glyph.length;
        }

        // all the points along the center are colinear, and to give an accurate measurement of the "size" of a glyph,
        // the merged line across is more meaningful.
        final GlyphEdge[] centerLine = GlyphEdge.CENTER_LINE;

        int simplifiedSize = glyph.length;

        int mergedLength = 0;

        for (final GlyphEdge edge : centerLine) {
            if (containsEdge(glyph, edge)) {
                mergedLength++;
                simplifiedSize--;
            } else {
                if (mergedLength > 0) {
                    mergedLength = 0;
                    simplifiedSize++;
                }
            }
        }

        if (mergedLength > 0) {
            simplifiedSize++;
        }

        return simplifiedSize;
    }

    static int edgesToBits(final GlyphEdge[] glyph) {
        int mask = 0;
        for (final GlyphEdge edge : glyph) {
            mask |= edge.asBit();
        }
        return mask;
    }

    static GlyphEdge[] bitsToEdges(final int bits) {
        final List<GlyphEdge> edges = new ArrayList<>(GlyphEdge.EDGES.length);
        for (final GlyphEdge edge : GlyphEdge.EDGES) {
            if ((bits & edge.asBit()) != 0) {
                edges.add(edge);
            }
        }
        return edges.toArray(new GlyphEdge[0]);
    }

    private static boolean canSimplify(final GlyphEdge[] glyph) {
        // we can potentially simplify if we have 2+ edges in the center
        int count = 0;
        for (final GlyphEdge edge : glyph) {
            if (isCenterEdge(edge)) {
                if (++count >= 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCenterEdge(final GlyphEdge edge) {
        return containsEdge(GlyphEdge.CENTER_LINE, edge);
    }

    static boolean containsEdge(final GlyphEdge[] glyph, final GlyphEdge edge) {
        for (final GlyphEdge other : glyph) {
            if (edge == other) {
                return true;
            }
        }
        return false;
    }

    public int asBits() {
        return bits;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof GlyphShape && ((GlyphShape) obj).bits == bits;
    }

    @Override
    public int hashCode() {
        return bits * 31;
    }
}
