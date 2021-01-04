package dev.gegy.magic.client.glyph.spellcasting.outline;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.plane.GlyphPlane;
import net.minecraft.client.network.ClientPlayerEntity;

public final class GlyphOutline {
    public final GlyphPlane plane;
    public final float radius;

    GlyphOutline(GlyphPlane plane, float radius) {
        this.plane = plane;
        this.radius = radius;
    }

    public ClientGlyph createGlyph(ClientPlayerEntity source) {
        return new ClientGlyph(source, this.plane, this.radius, source.world.getTime());
    }
}
