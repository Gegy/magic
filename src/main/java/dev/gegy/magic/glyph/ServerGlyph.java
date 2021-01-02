package dev.gegy.magic.glyph;

import net.minecraft.server.network.ServerPlayerEntity;

public final class ServerGlyph {
    private final int networkId;
    private final ServerPlayerEntity source;
    private final GlyphPlane plane;
    private final float radius;

    private int shape;

    ServerGlyph(int networkId, ServerPlayerEntity source, GlyphPlane plane, float radius) {
        this.networkId = networkId;
        this.source = source;
        this.plane = plane;
        this.radius = radius;
    }

    public int getNetworkId() {
        return this.networkId;
    }

    public ServerPlayerEntity getSource() {
        return this.source;
    }

    public GlyphPlane getPlane() {
        return this.plane;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public int getShape() {
        return this.shape;
    }
}
