package dev.gegy.magic.glyph.shape;

import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public enum GlyphNode {
    TOP(true, 0.0f, 1.0f),
    BOTTOM(true, 0.0f, -1.0f),
    SIDE_UPPER(true, (float) Math.sqrt(0.75f), 0.5f),
    SIDE_LOWER(true, (float) Math.sqrt(0.75f), -0.5f),
    CENTER_UPPER(false, 0.0f, 0.5f),
    CENTER(false, 0.0f, 0.0f),
    CENTER_LOWER(false, 0.0f, -0.5f);

    public static final PacketCodec<@Nullable GlyphNode> PACKET_CODEC = PacketCodec.of(
            (node, buf) -> {
                int id = node != null ? node.ordinal() : 0xFF;
                buf.writeByte(id & 0xFF);
            },
            buf -> GlyphNode.byId(buf.readUnsignedByte())
    );

    public static final GlyphNode[] NODES = values();

    public static final GlyphNode[] CIRCUMFERENCE;

    static {
        final List<GlyphNode> circumference = new ArrayList<>();
        for (final GlyphNode node : NODES) {
            if (node.circumference) {
                circumference.add(node);
            }
        }

        CIRCUMFERENCE = circumference.toArray(new GlyphNode[0]);
    }

    private final boolean circumference;
    private final Vec2 point;

    GlyphNode(final boolean circumference, final float x, final float y) {
        this.circumference = circumference;
        point = new Vec2(x, y);
    }

    public boolean isAtCircumference() {
        return circumference;
    }

    public Vec2 getPoint() {
        return point;
    }

    @Nullable
    public static GlyphNode byId(final short nodeId) {
        if (nodeId >= 0 && nodeId < GlyphNode.NODES.length) {
            return GlyphNode.NODES[nodeId];
        }
        return null;
    }
}
