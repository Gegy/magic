package dev.gegy.magic.client.spellcasting.state.outline;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.transform.GlyphPlane;
import net.minecraft.client.network.ClientPlayerEntity;

public record GlyphOutline(GlyphPlane plane, float radius) {
    public ClientGlyph createGlyph(ClientPlayerEntity source) {
        return new ClientGlyph(source, this.plane, this.radius, source.world.getTime());
    }
}
