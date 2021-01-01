package dev.gegy.magic.glyph.shape;

import net.minecraft.util.math.Vec2f;

import java.util.ArrayList;
import java.util.List;

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
}
