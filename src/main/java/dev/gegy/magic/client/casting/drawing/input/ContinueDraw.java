package dev.gegy.magic.client.casting.drawing.input;

import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.casting.drawing.input.outline.GlyphOutline;
import dev.gegy.magic.client.casting.drawing.input.outline.GlyphOutlineTracker;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import net.minecraft.entity.player.PlayerEntity;

final class ContinueDraw implements DrawingInputState {
    private static final int SAMPLE_INTERVAL = 2;
    private static final int SAMPLE_PERIOD = 80;
    private static final int SAMPLE_BUFFER_SIZE = SAMPLE_PERIOD / SAMPLE_INTERVAL;

    private final GlyphOutlineTracker outlineTracker = new GlyphOutlineTracker(SAMPLE_BUFFER_SIZE);

    @Override
    public DrawingInputState tick(ClientCastingDrawing casting, PlayerEntity player) {
        // TODO: crude detection
        if (player.handSwinging) {
            casting.senders().prepareSpell();
            return new BeginDraw();
        }

        if (player.age % SAMPLE_INTERVAL == 0) {
            GlyphOutline outline = this.outlineTracker.pushSample(player.getRotationVec(1.0F));
            if (outline != null) {
                ClientDrawingGlyph glyph = outline.createGlyph(player);
                casting.senders().beginGlyph(outline.plane().direction(), outline.radius());
                return new DrawGlyph.OutsideCircle(glyph, outline.plane());
            }
        }

        return this;
    }
}
