package dev.gegy.magic.casting.drawing.event;

import dev.gegy.magic.casting.drawing.event.c2s.BeginGlyphC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.CancelGlyphC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.DrawGlyphShapeC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.DrawGlyphStrokeC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.PrepareSpellC2SEvent;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.NetworkSender;
import org.joml.Vector3f;

public final class ClientDrawingEventSenders {
    private final NetworkSender<BeginGlyphC2SEvent> beginGlyph;
    private final NetworkSender<CancelGlyphC2SEvent> cancelGlyph;
    private final NetworkSender<DrawGlyphShapeC2SEvent> drawGlyphShape;
    private final NetworkSender<DrawGlyphStrokeC2SEvent> drawGlyphStroke;
    private final NetworkSender<PrepareSpellC2SEvent> prepareSpell;

    private ClientDrawingEventSenders(
            final NetworkSender<BeginGlyphC2SEvent> beginGlyph,
            final NetworkSender<CancelGlyphC2SEvent> cancelGlyph,
            final NetworkSender<DrawGlyphShapeC2SEvent> drawGlyphShape,
            final NetworkSender<DrawGlyphStrokeC2SEvent> drawGlyphStroke,
            final NetworkSender<PrepareSpellC2SEvent> prepareSpell
    ) {
        this.beginGlyph = beginGlyph;
        this.cancelGlyph = cancelGlyph;
        this.drawGlyphShape = drawGlyphShape;
        this.drawGlyphStroke = drawGlyphStroke;
        this.prepareSpell = prepareSpell;
    }

    public static ClientDrawingEventSenders registerTo(final ClientCastingBuilder casting) {
        return new ClientDrawingEventSenders(
                casting.registerOutboundEvent(BeginGlyphC2SEvent.SPEC),
                casting.registerOutboundEvent(CancelGlyphC2SEvent.SPEC),
                casting.registerOutboundEvent(DrawGlyphShapeC2SEvent.SPEC),
                casting.registerOutboundEvent(DrawGlyphStrokeC2SEvent.SPEC),
                casting.registerOutboundEvent(PrepareSpellC2SEvent.SPEC)
        );
    }

    public void beginGlyph(final Vector3f direction, final float radius) {
        beginGlyph.send(new BeginGlyphC2SEvent(direction, radius));
    }

    public void cancelGlyph() {
        cancelGlyph.send(new CancelGlyphC2SEvent());
    }

    public void drawGlyphShape(final int shape) {
        drawGlyphShape.send(new DrawGlyphShapeC2SEvent(shape));
    }

    public void startGlyphStroke(final GlyphNode node) {
        drawGlyphStroke.send(DrawGlyphStrokeC2SEvent.start(node));
    }

    public void stopGlyphStroke() {
        drawGlyphStroke.send(DrawGlyphStrokeC2SEvent.stop());
    }

    public void prepareSpell() {
        prepareSpell.send(new PrepareSpellC2SEvent());
    }
}
