package dev.gegy.magic.casting.drawing.event;

import dev.gegy.magic.casting.drawing.event.c2s.BeginGlyphC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.CancelGlyphC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.DrawGlyphShapeC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.DrawGlyphStrokeC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.PrepareSpellC2SEvent;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.NetworkSender;
import net.minecraft.util.math.Vec3f;

public final class ClientDrawingEventSenders {
    private final NetworkSender<BeginGlyphC2SEvent> beginGlyph;
    private final NetworkSender<CancelGlyphC2SEvent> cancelGlyph;
    private final NetworkSender<DrawGlyphShapeC2SEvent> drawGlyphShape;
    private final NetworkSender<DrawGlyphStrokeC2SEvent> drawGlyphStroke;
    private final NetworkSender<PrepareSpellC2SEvent> prepareSpell;

    private ClientDrawingEventSenders(
            NetworkSender<BeginGlyphC2SEvent> beginGlyph,
            NetworkSender<CancelGlyphC2SEvent> cancelGlyph,
            NetworkSender<DrawGlyphShapeC2SEvent> drawGlyphShape,
            NetworkSender<DrawGlyphStrokeC2SEvent> drawGlyphStroke,
            NetworkSender<PrepareSpellC2SEvent> prepareSpell
    ) {
        this.beginGlyph = beginGlyph;
        this.cancelGlyph = cancelGlyph;
        this.drawGlyphShape = drawGlyphShape;
        this.drawGlyphStroke = drawGlyphStroke;
        this.prepareSpell = prepareSpell;
    }

    public static ClientDrawingEventSenders registerTo(ClientCastingBuilder casting) {
        return new ClientDrawingEventSenders(
                casting.registerOutboundEvent(BeginGlyphC2SEvent.SPEC),
                casting.registerOutboundEvent(CancelGlyphC2SEvent.SPEC),
                casting.registerOutboundEvent(DrawGlyphShapeC2SEvent.SPEC),
                casting.registerOutboundEvent(DrawGlyphStrokeC2SEvent.SPEC),
                casting.registerOutboundEvent(PrepareSpellC2SEvent.SPEC)
        );
    }

    public void beginGlyph(Vec3f direction, float radius) {
        this.beginGlyph.send(new BeginGlyphC2SEvent(direction, radius));
    }

    public void cancelGlyph() {
        this.cancelGlyph.send(new CancelGlyphC2SEvent());
    }

    public void drawGlyphShape(int shape) {
        this.drawGlyphShape.send(new DrawGlyphShapeC2SEvent(shape));
    }

    public void startGlyphStroke(GlyphNode node) {
        this.drawGlyphStroke.send(DrawGlyphStrokeC2SEvent.start(node));
    }

    public void stopGlyphStroke() {
        this.drawGlyphStroke.send(DrawGlyphStrokeC2SEvent.stop());
    }

    public void prepareSpell() {
        this.prepareSpell.send(new PrepareSpellC2SEvent());
    }
}
