package dev.gegy.magic.casting.drawing.event;

import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.event.s2c.DrawGlyphS2CEvent;
import dev.gegy.magic.casting.drawing.event.s2c.CancelDrawingS2CEvent;
import dev.gegy.magic.casting.drawing.event.s2c.UpdateDrawingS2CEvent;
import dev.gegy.magic.casting.drawing.ServerDrawingGlyph;
import dev.gegy.magic.network.NetworkSender;
import org.jetbrains.annotations.NotNull;

public final class ServerDrawingEventSenders {
    private final NetworkSender<DrawGlyphS2CEvent> drawGlyph;
    private final NetworkSender<UpdateDrawingS2CEvent> updateDrawing;
    private final NetworkSender<CancelDrawingS2CEvent> cancelDrawing;

    private ServerDrawingEventSenders(
            NetworkSender<DrawGlyphS2CEvent> drawGlyph,
            NetworkSender<UpdateDrawingS2CEvent> updateDrawing,
            NetworkSender<CancelDrawingS2CEvent> cancelDrawing
    ) {
        this.drawGlyph = drawGlyph;
        this.updateDrawing = updateDrawing;
        this.cancelDrawing = cancelDrawing;
    }

    public static ServerDrawingEventSenders registerTo(ServerCastingBuilder casting) {
        return new ServerDrawingEventSenders(
                casting.registerOutboundEvent(DrawGlyphS2CEvent.SPEC),
                casting.registerOutboundEvent(UpdateDrawingS2CEvent.SPEC),
                casting.registerOutboundEvent(CancelDrawingS2CEvent.SPEC)
        );
    }

    public void drawGlyph(ServerDrawingGlyph glyph) {
        this.drawGlyph.broadcast(new DrawGlyphS2CEvent(glyph.asParameters()));
    }

    public void cancelDrawing() {
        this.cancelDrawing.broadcast(new CancelDrawingS2CEvent());
    }

    public void broadcastUpdateDrawing(ServerDrawingGlyph glyph) {
        this.updateDrawing.broadcast(this.makeUpdateDrawing(glyph));
    }

    public void sendUpdateDrawing(ServerDrawingGlyph glyph) {
        this.updateDrawing.send(this.makeUpdateDrawing(glyph));
    }

    @NotNull
    private UpdateDrawingS2CEvent makeUpdateDrawing(ServerDrawingGlyph glyph) {
        return new UpdateDrawingS2CEvent(glyph.getShape(), glyph.getStroke(), glyph.getFormedType());
    }
}
