package dev.gegy.magic.glyph;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.util.math.Vec2f;

import java.util.*;

public enum GlyphNode {
    TOP(true,
            new Vec2f(0.0F, 1.0F)
    ),
    BOTTOM(true,
            new Vec2f(0.0F, -1.0F)
    ),
    SIDE_UPPER(true,
            new Vec2f((float) Math.sqrt(0.75F), 0.5F),
            new Vec2f((float) -Math.sqrt(0.75F), 0.5F)
    ),
    SIDE_LOWER(true,
            new Vec2f((float) Math.sqrt(0.75F), -0.5F),
            new Vec2f((float) -Math.sqrt(0.75F), -0.5F)
    ),
    CENTER_UPPER(false,
            new Vec2f(0.0F, 0.5F)
    ),
    CENTER(false,
            new Vec2f(0.0F, 0.0F)
    ),
    CENTER_LOWER(false,
            new Vec2f(0.0F, -0.5F)
    );

    public static final GlyphNode[] NODES = values();
    public static final GlyphNode[] CENTER_LINE = new GlyphNode[] {
            TOP, CENTER_UPPER, CENTER, CENTER_LOWER, BOTTOM
    };

    public static final GlyphNode[] CIRCUMFERENCE;

    private static final GlyphEdge[] EDGES = new GlyphEdge[] {
            new GlyphEdge(GlyphNode.CENTER, GlyphNode.CENTER_UPPER),
            new GlyphEdge(GlyphNode.CENTER, GlyphNode.CENTER_LOWER),
            new GlyphEdge(GlyphNode.CENTER, GlyphNode.SIDE_UPPER),
            new GlyphEdge(GlyphNode.CENTER, GlyphNode.SIDE_LOWER),

            new GlyphEdge(GlyphNode.CENTER_UPPER, GlyphNode.SIDE_UPPER),
            new GlyphEdge(GlyphNode.CENTER_UPPER, GlyphNode.SIDE_LOWER),
            new GlyphEdge(GlyphNode.CENTER_LOWER, GlyphNode.SIDE_UPPER),
            new GlyphEdge(GlyphNode.CENTER_LOWER, GlyphNode.SIDE_LOWER),

            new GlyphEdge(GlyphNode.TOP, GlyphNode.CENTER_UPPER),
            new GlyphEdge(GlyphNode.TOP, GlyphNode.SIDE_UPPER),
            new GlyphEdge(GlyphNode.TOP, GlyphNode.SIDE_LOWER),

            new GlyphEdge(GlyphNode.BOTTOM, GlyphNode.CENTER_LOWER),
            new GlyphEdge(GlyphNode.BOTTOM, GlyphNode.SIDE_UPPER),
            new GlyphEdge(GlyphNode.BOTTOM, GlyphNode.SIDE_LOWER),

            new GlyphEdge(GlyphNode.SIDE_LOWER, GlyphNode.SIDE_UPPER),
    };

    static final Map<GlyphNode, GlyphNode[]> CONNECTIONS = new EnumMap<>(GlyphNode.class);

    static {
        Multimap<GlyphNode, GlyphNode> connections = HashMultimap.create();
        for (GlyphEdge edge : EDGES) {
            connections.put(edge.from, edge.to);
            connections.put(edge.to, edge.from);
        }
        for (Map.Entry<GlyphNode, Collection<GlyphNode>> entry : connections.asMap().entrySet()) {
            CONNECTIONS.put(entry.getKey(), entry.getValue().toArray(new GlyphNode[0]));
        }

        List<GlyphNode> circumference = new ArrayList<>();
        for (GlyphNode node : NODES) {
            if (node.circumference) {
                circumference.add(node);
            }
        }

        CIRCUMFERENCE = circumference.toArray(new GlyphNode[0]);
    }

    private final boolean circumference;
    private final Vec2f[] points;

    GlyphNode(boolean circumference, Vec2f... points) {
        this.circumference = circumference;
        this.points = points;
    }

    public boolean isAtCircumference() {
        return this.circumference;
    }

    public Vec2f[] getPoints() {
        return this.points;
    }

    public GlyphNode[] getConnections() {
        return CONNECTIONS.get(this);
    }
}
