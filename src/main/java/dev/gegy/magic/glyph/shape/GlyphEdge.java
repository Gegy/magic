package dev.gegy.magic.glyph.shape;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    static final Map<GlyphNode, GlyphNode[]> CONNECTED_NODES = new EnumMap<>(GlyphNode.class);
    static final Map<GlyphNode, GlyphEdge[]> CONNECTED_EDGES = new EnumMap<>(GlyphNode.class);

    static {
        Multimap<GlyphNode, GlyphEdge> connectedEdges = HashMultimap.create();
        Multimap<GlyphNode, GlyphNode> connectedNodes = HashMultimap.create();
        for (GlyphEdge edge : EDGES) {
            connectedEdges.put(edge.from, edge);
            connectedEdges.put(edge.to, edge);
            connectedNodes.put(edge.from, edge.to);
            connectedNodes.put(edge.to, edge.from);
        }
        for (GlyphNode node : GlyphNode.NODES) {
            CONNECTED_EDGES.put(node, connectedEdges.get(node).toArray(new GlyphEdge[0]));
            CONNECTED_NODES.put(node, connectedNodes.get(node).toArray(new GlyphNode[0]));
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

    @Nullable
    public static GlyphEdge between(GlyphNode from, GlyphNode to) {
        GlyphEdge[] edges = getConnectedEdgesTo(from);
        for (GlyphEdge edge : edges) {
            if (edge.contains(to)) {
                return edge;
            }
        }
        return null;
    }

    public int asBit() {
        return 1 << this.ordinal();
    }

    public boolean contains(GlyphNode node) {
        return node == this.from || node == this.to;
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

    @NotNull
    public static GlyphEdge[] getConnectedEdgesTo(GlyphNode node) {
        return CONNECTED_EDGES.get(node);
    }

    @NotNull
    public static GlyphNode[] getConnectedNodesTo(GlyphNode node) {
        return CONNECTED_NODES.get(node);
    }
}
