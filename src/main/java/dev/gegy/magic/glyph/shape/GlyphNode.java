package dev.gegy.magic.glyph.shape;

import net.minecraft.util.math.Vec2f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum GlyphNode {
    TOP(true, 0.0F, 1.0F),
    BOTTOM(true, 0.0F, -1.0F),
    SIDE_UPPER(true, (float) Math.sqrt(0.75F), 0.5F),
    SIDE_LOWER(true, (float) Math.sqrt(0.75F), -0.5F),
    CENTER_UPPER(false, 0.0F, 0.5F),
    CENTER(false, 0.0F, 0.0F),
    CENTER_LOWER(false, 0.0F, -0.5F);

    public static final GlyphNode[] NODES = values();

    public static final GlyphNode[] CIRCUMFERENCE;

    static {
        List<GlyphNode> circumference = new ArrayList<>();
        for (GlyphNode node : NODES) {
            if (node.circumference) {
                circumference.add(node);
            }
        }

        CIRCUMFERENCE = circumference.toArray(new GlyphNode[0]);
    }

    private final boolean circumference;
    private final Vec2f point;

    GlyphNode(boolean circumference, float x, float y) {
        this.circumference = circumference;
        this.point = new Vec2f(x, y);
    }

    public boolean isAtCircumference() {
        return this.circumference;
    }

    public Vec2f getPoint() {
        return this.point;
    }

    @Nullable
    public static GlyphNode byId(short nodeId) {
        if (nodeId >= 0 && nodeId < GlyphNode.NODES.length) {
            return GlyphNode.NODES[nodeId];
        }
        return null;
    }
}
