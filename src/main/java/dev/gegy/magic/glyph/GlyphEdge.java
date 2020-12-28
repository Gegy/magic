package dev.gegy.magic.glyph;

public final class GlyphEdge {
    public final GlyphNode from;
    public final GlyphNode to;

    public GlyphEdge(GlyphNode a, GlyphNode b) {
        if (a.ordinal() < b.ordinal()) {
            this.from = a;
            this.to = b;
        } else {
            this.from = b;
            this.to = a;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof GlyphEdge) {
            GlyphEdge edge = (GlyphEdge) obj;
            return this.from == edge.from && this.to == edge.to;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.from.hashCode() + this.to.hashCode() * 31;
    }
}
