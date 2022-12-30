package dev.gegy.magic.client.casting.drawing.input;

import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.casting.drawing.input.outline.GlyphOutline;
import dev.gegy.magic.client.casting.drawing.input.outline.GlyphOutlineTracker;
import net.minecraft.world.entity.player.Player;

final class ContinueDraw implements DrawingInputState {
    private static final int SAMPLE_INTERVAL = 2;
    private static final int SAMPLE_PERIOD = 80;
    private static final int SAMPLE_BUFFER_SIZE = SAMPLE_PERIOD / SAMPLE_INTERVAL;

    private final GlyphOutlineTracker outlineTracker = new GlyphOutlineTracker(SAMPLE_BUFFER_SIZE);

    @Override
    public DrawingInputState tick(ClientCastingDrawing casting, Player player) {
        // TODO: crude detection
        if (player.swinging) {
            casting.senders().prepareSpell();
            return new BeginDraw();
        }

        if (player.tickCount % SAMPLE_INTERVAL == 0) {
            GlyphOutline outline = this.outlineTracker.pushSample(player.getViewVector(1.0F));
            if (outline != null) {
                ClientDrawingGlyph glyph = outline.createGlyph(player);
                casting.senders().beginGlyph(outline.plane().direction(), outline.radius());
                return new DrawGlyph.OutsideCircle(glyph, outline.plane());
            }
        }

        return this;
    }
}
