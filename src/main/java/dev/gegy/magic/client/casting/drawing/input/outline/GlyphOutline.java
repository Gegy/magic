package dev.gegy.magic.client.casting.drawing.input.outline;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import net.minecraft.entity.player.PlayerEntity;

public record GlyphOutline(GlyphPlane plane, float radius) {
    public ClientDrawingGlyph createGlyph(PlayerEntity source) {
        return new ClientDrawingGlyph(SpellSource.of(source), this.plane, this.radius);
    }
}
