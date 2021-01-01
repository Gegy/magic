package dev.gegy.magic.glyph.shape;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

public enum GlyphEdge {
    CENTER_TO_CENTER_UPPER(GlyphNode.CENTER, GlyphNode.CENTER_UPPER),
    CENTER_TO_CENTER_LOWER(GlyphNode.CENTER, GlyphNode.CENTER_LOWER),
    CENTER_TO_SIDE_UPPER(GlyphNode.CENTER, GlyphNode.SIDE_UPPER),
    CENTER_TO_SIDE_LOWER(GlyphNode.CENTER, GlyphNode.SIDE_LOWER),

    CENTER_UPPER_TO_SIDE_UPPER(GlyphNode.CENTER_UPPER, GlyphNode.SIDE_UPPER),
    CENTER_UPPER_TO_SIDE_LOWER(GlyphNode.CENTER_UPPER, GlyphNode.SIDE_LOWER),
    CENTER_LOWER_TO_SIDE_UPPER(GlyphNode.CENTER_LOWER, GlyphNode.SIDE_UPPER),
    CENTER_LOWER_TO_SIDE_LOWER(GlyphNode.CENTER_LOWER, GlyphNode.SIDE_LOWER),

    TOP_TO_CENTER_UPPER(GlyphNode.TOP, GlyphNode.CENTER_UPPER),
    TOP_TO_SIDE_UPPER(GlyphNode.TOP, GlyphNode.SIDE_UPPER),
    TOP_TO_SIDE_LOWER(GlyphNode.TOP, GlyphNode.SIDE_LOWER),

    BOTTOM_TO_CENTER_LOWER(GlyphNode.BOTTOM, GlyphNode.CENTER_LOWER),
    BOTTOM_TO_SIDE_UPPER(GlyphNode.BOTTOM, GlyphNode.SIDE_UPPER),
    BOTTOM_TO_SIDE_LOWER(GlyphNode.BOTTOM, GlyphNode.SIDE_LOWER),

    SIDE_LOWER_TO_SIDE_UPPER(GlyphNode.SIDE_LOWER, GlyphNode.SIDE_UPPER);

    public static final GlyphEdge[] EDGES = values();

    public static final GlyphEdge[] CENTER_LINE = new GlyphEdge[] {
            TOP_TO_CENTER_UPPER, CENTER_TO_CENTER_UPPER, CENTER_TO_CENTER_LOWER, BOTTOM_TO_CENTER_LOWER
    };

    static final Map<GlyphNode, GlyphEdge[]> CONNECTIONS = new EnumMap<>(GlyphNode.class);

    static {
        Multimap<GlyphNode, GlyphEdge> connections = HashMultimap.create();
        for (GlyphEdge edge : EDGES) {
            connections.put(edge.from, edge);
            connections.put(edge.to, edge);
        }
        for (Map.Entry<GlyphNode, Collection<GlyphEdge>> entry : connections.asMap().entrySet()) {
            CONNECTIONS.put(entry.getKey(), entry.getValue().toArray(new GlyphEdge[0]));
        }
    }

    public final GlyphNode from;
    public final GlyphNode to;

    GlyphEdge(GlyphNode a, GlyphNode b) {
        if (a.ordinal() < b.ordinal()) {
            this.from = a;
            this.to = b;
        } else {
            this.from = b;
            this.to = a;
        }
    }

    public int asBit() {
        return 1 << this.ordinal();
    }

    public GlyphNode getOther(GlyphNode node) {
        if (node == this.from) {
            return this.to;
        } else if (node == this.to) {
            return this.from;
        } else {
            throw new IllegalArgumentException(node + " is not apart of " + this);
        }
    }

    public static GlyphEdge[] getConnectionsTo(GlyphNode node) {
        return CONNECTIONS.get(node);
    }
}
