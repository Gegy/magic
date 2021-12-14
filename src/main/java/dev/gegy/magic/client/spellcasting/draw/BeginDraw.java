package dev.gegy.magic.client.spellcasting.draw;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.spellcasting.draw.outline.GlyphOutline;
import dev.gegy.magic.client.spellcasting.draw.outline.GlyphOutlineTracker;
import dev.gegy.magic.network.c2s.BeginGlyphC2SPacket;
import net.minecraft.client.network.ClientPlayerEntity;

final class BeginDraw implements SpellDrawState {
    private static final int SAMPLE_INTERVAL = 2;
    private static final int SAMPLE_PERIOD = 80;
    private static final int SAMPLE_BUFFER_SIZE = SAMPLE_PERIOD / SAMPLE_INTERVAL;

    private final GlyphOutlineTracker outlineTracker = new GlyphOutlineTracker(SAMPLE_BUFFER_SIZE);

    @Override
    public SpellDrawState tick(ClientPlayerEntity player) {
        if (player.age % SAMPLE_INTERVAL == 0) {
            GlyphOutline outline = this.outlineTracker.pushSample(player.getRotationVec(1.0F));
            if (outline != null) {
                ClientGlyph glyph = outline.createGlyph(player);
                BeginGlyphC2SPacket.sendToServer(outline.plane.getDirection(), outline.radius);
                return new DrawGlyph.OutsideCircle(glyph, outline.plane);
            }
        }

        return this;
    }
}
