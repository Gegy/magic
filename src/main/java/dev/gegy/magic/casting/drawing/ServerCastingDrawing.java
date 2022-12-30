package dev.gegy.magic.casting.drawing;

import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.event.ServerDrawingEventSenders;
import dev.gegy.magic.casting.drawing.event.c2s.BeginGlyphC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.CancelGlyphC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.DrawGlyphShapeC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.DrawGlyphStrokeC2SEvent;
import dev.gegy.magic.casting.drawing.event.c2s.PrepareSpellC2SEvent;
import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.client.casting.ClientCastingType;
import dev.gegy.magic.client.glyph.spell.SpellCasting;
import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphShapeStorage;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ServerCastingDrawing {
    private final ServerPlayer player;
    private final ServerDrawingEventSenders senders;

    private final List<ServerDrawingGlyph> glyphs = new ArrayList<>();
    private ServerDrawingGlyph drawing;

    private boolean preparingSpell;

    private ServerCastingDrawing(final ServerPlayer player, final ServerDrawingEventSenders senders) {
        this.player = player;
        this.senders = senders;
    }

    public static ServerCasting build(final ServerPlayer player, final ServerCastingBuilder casting) {
        final ServerDrawingEventSenders senders = ServerDrawingEventSenders.registerTo(casting);
        final ServerCastingDrawing drawing = new ServerCastingDrawing(player, senders);

        casting.registerClientCasting(ClientCastingType.DRAWING, drawing::buildParameters);

        casting.bindInboundEvent(BeginGlyphC2SEvent.SPEC, drawing::beginGlyph);
        casting.bindInboundEvent(CancelGlyphC2SEvent.SPEC, drawing::cancelGlyph);
        casting.bindInboundEvent(DrawGlyphShapeC2SEvent.SPEC, drawing::drawGlyphShape);
        casting.bindInboundEvent(DrawGlyphStrokeC2SEvent.SPEC, drawing::drawGlyphStroke);

        casting.bindInboundEvent(PrepareSpellC2SEvent.SPEC, event -> drawing.preparingSpell = true);

        casting.registerTicker(drawing::tick);

        return casting.build();
    }

    private void beginGlyph(final BeginGlyphC2SEvent event) {
        // TODO: input validation on glyphs?
        final ServerDrawingGlyph glyph = new ServerDrawingGlyph(event.direction(), event.radius());

        glyphs.add(glyph);
        drawing = glyph;
        senders.drawGlyph(glyph);
    }

    private void cancelGlyph(final CancelGlyphC2SEvent event) {
        final ServerDrawingGlyph glyph = drawing;
        if (glyph != null) {
            drawing = null;
            glyphs.remove(glyph);
            senders.cancelDrawing();
        }
    }

    private void drawGlyphShape(final DrawGlyphShapeC2SEvent event) {
        final ServerDrawingGlyph glyph = drawing;
        if (glyph != null) {
            glyph.setShape(event.shape());

            if (glyph.tryForm(GlyphShapeStorage.get(player.server))) {
                senders.sendUpdateDrawing(glyph);
            }

            senders.broadcastUpdateDrawing(glyph);
        }
    }

    private void drawGlyphStroke(final DrawGlyphStrokeC2SEvent event) {
        final ServerDrawingGlyph glyph = drawing;
        if (glyph != null) {
            glyph.setStroke(event.node());
            senders.broadcastUpdateDrawing(glyph);
        }
    }

    @Nullable
    private ServerCasting.Factory tick() {
        if (preparingSpell) {
            final List<GlyphType> glyphTypes = glyphs.stream()
                    .map(ServerDrawingGlyph::getFormedType)
                    .filter(Objects::nonNull)
                    .toList();

            final GlyphType.CastFunction cast = SpellCasting.cast(glyphTypes);
            if (cast != null) {
                final SpellParameters spell = buildSpellParameters();
                return (player, casting) -> cast.build(player, spell, casting);
            } else {
                return ServerCastingDrawing::build;
            }
        }

        return null;
    }

    private SpellParameters buildSpellParameters() {
        final List<GlyphForm> glyphs = this.glyphs.stream()
                .map(ServerDrawingGlyph::asForm)
                .filter(Objects::nonNull)
                .toList();
        final Vector3f direction = player.getViewVector(1.0f).toVector3f();

        return new SpellParameters(glyphs, direction);
    }

    private DrawingParameters buildParameters() {
        final ArrayList<DrawingGlyphParameters> glyphs = new ArrayList<DrawingGlyphParameters>(this.glyphs.size());
        for (final ServerDrawingGlyph glyph : this.glyphs) {
            glyphs.add(glyph.asParameters());
        }
        return new DrawingParameters(glyphs);
    }
}
