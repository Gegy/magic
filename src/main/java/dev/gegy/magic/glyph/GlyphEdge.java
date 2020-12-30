package dev.gegy.magic.glyph;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public final class GlyphEdge {
    public static final Codec<GlyphEdge> CODEC = GlyphNode.CODEC.listOf().comapFlatMap(
            nodes -> {
                if (nodes.size() == 2) {
                    return DataResult.success(new GlyphEdge(nodes.get(0), nodes.get(1)));
                } else {
                    return DataResult.error("Expected 2 nodes for edge");
                }
            },
            edge -> ImmutableList.of(edge.from, edge.to)
    );

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
