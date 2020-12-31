package dev.gegy.magic.glyph.shape;

import com.mojang.serialization.Codec;

import java.util.Arrays;

public final class GlyphShape {
    public static final Codec<GlyphShape> CODEC = GlyphEdge.CODEC.listOf()
            .xmap(edges -> new GlyphShape(edges.toArray(new GlyphEdge[0])), glyph -> Arrays.asList(glyph.edges));

    public final GlyphEdge[] edges;
    public final int size;

    public GlyphShape(GlyphEdge[] edges, int size) {
        this.edges = edges;
        this.size = size;
    }

    public GlyphShape(GlyphEdge[] edges) {
        this(edges, getSize(edges));
    }

    static int getSize(GlyphEdge[] glyph) {
        if (!canSimplify(glyph)) {
            return glyph.length;
        }

        // all the points along the center are colinear, and to give an accurate measurement of the "size" of a glyph,
        // the merged line across is more meaningful.
        GlyphNode[] centerLine = GlyphNode.CENTER_LINE;

        int simplifiedSize = glyph.length;

        GlyphNode from = null;

        for (int i = 0; i < centerLine.length - 1; i++) {
            GlyphEdge edge = new GlyphEdge(centerLine[i], centerLine[i + 1]);
            if (containsEdge(glyph, edge)) {
                if (from == null) {
                    from = centerLine[i];
                }
            } else {
                if (from != null) {
                    simplifiedSize++;
                    from = null;
                }
            }
        }

        if (from != null) {
            simplifiedSize++;
        }

        return simplifiedSize;
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

    static boolean containsEdge(GlyphEdge[] glyph, GlyphEdge edge) {
        for (GlyphEdge other : glyph) {
            if (other.equals(edge)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof GlyphShape && this.equals((GlyphShape) obj);
    }

    private boolean equals(GlyphShape glyph) {
        if (glyph.edges.length != this.edges.length) {
            return false;
        }

        // evaluate whether the glyphs are equal independent of order!
        for (GlyphEdge edge : this.edges) {
            boolean matched = false;
            for (GlyphEdge otherEdge : glyph.edges) {
                if (edge.equals(otherEdge)) {
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        // hash must to be commutative given order-independent equality!
        int hash = this.edges.length * 31;
        for (GlyphEdge edge : this.edges) {
            hash += edge.hashCode();
        }
        return hash;
    }
}
