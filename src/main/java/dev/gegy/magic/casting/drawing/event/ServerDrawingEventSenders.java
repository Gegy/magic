package dev.gegy.magic.casting.drawing.event;

import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.ServerDrawingGlyph;
import dev.gegy.magic.casting.drawing.event.s2c.CancelDrawingS2CEvent;
import dev.gegy.magic.casting.drawing.event.s2c.DrawGlyphS2CEvent;
import dev.gegy.magic.casting.drawing.event.s2c.UpdateDrawingS2CEvent;
import dev.gegy.magic.network.NetworkSender;
import org.jetbrains.annotations.NotNull;

public final class ServerDrawingEventSenders {
    private final NetworkSender<DrawGlyphS2CEvent> drawGlyph;
    private final NetworkSender<UpdateDrawingS2CEvent> updateDrawing;
    private final NetworkSender<CancelDrawingS2CEvent> cancelDrawing;

    private ServerDrawingEventSenders(
            final NetworkSender<DrawGlyphS2CEvent> drawGlyph,
            final NetworkSender<UpdateDrawingS2CEvent> updateDrawing,
            final NetworkSender<CancelDrawingS2CEvent> cancelDrawing
    ) {
        this.drawGlyph = drawGlyph;
        this.updateDrawing = updateDrawing;
        this.cancelDrawing = cancelDrawing;
    }

    public static ServerDrawingEventSenders registerTo(final ServerCastingBuilder casting) {
        return new ServerDrawingEventSenders(
                casting.registerOutboundEvent(DrawGlyphS2CEvent.SPEC),
                casting.registerOutboundEvent(UpdateDrawingS2CEvent.SPEC),
                casting.registerOutboundEvent(CancelDrawingS2CEvent.SPEC)
        );
    }

    public void drawGlyph(final ServerDrawingGlyph glyph) {
        drawGlyph.broadcast(new DrawGlyphS2CEvent(glyph.asParameters()));
    }

    public void cancelDrawing() {
        cancelDrawing.broadcast(new CancelDrawingS2CEvent());
    }

    public void broadcastUpdateDrawing(final ServerDrawingGlyph glyph) {
        updateDrawing.broadcast(makeUpdateDrawing(glyph));
    }

    public void sendUpdateDrawing(final ServerDrawingGlyph glyph) {
        updateDrawing.send(makeUpdateDrawing(glyph));
    }

    @NotNull
    private UpdateDrawingS2CEvent makeUpdateDrawing(final ServerDrawingGlyph glyph) {
        return new UpdateDrawingS2CEvent(glyph.getShape(), glyph.getStroke(), glyph.getFormedType());
    }
}
