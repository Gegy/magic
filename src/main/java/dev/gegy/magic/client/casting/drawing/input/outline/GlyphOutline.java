package dev.gegy.magic.client.casting.drawing.input.outline;

import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import net.minecraft.world.entity.player.Player;

public record GlyphOutline(GlyphPlane plane, float radius) {
    public ClientDrawingGlyph createGlyph(final Player source) {
        return new ClientDrawingGlyph(SpellSource.of(source), plane, radius);
    }
}
