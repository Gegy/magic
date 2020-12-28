package dev.gegy.magic.glyph;

public final class Glyph {
    public final GlyphEdge[] edges;

    public Glyph(GlyphEdge[] edges) {
        this.edges = edges;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Glyph && this.equals((Glyph) obj);
    }

    private boolean equals(Glyph glyph) {
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
