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
import dev.gegy.magic.glyph.shape.GlyphShapeStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ServerCastingDrawing {
    private final ServerPlayerEntity player;
    private final ServerDrawingEventSenders senders;

    private final List<ServerDrawingGlyph> glyphs = new ArrayList<>();
    private ServerDrawingGlyph drawing;

    private boolean preparingSpell;

    private ServerCastingDrawing(ServerPlayerEntity player, ServerDrawingEventSenders senders) {
        this.player = player;
        this.senders = senders;
    }

    public static ServerCasting build(ServerPlayerEntity player, ServerCastingBuilder casting) {
        var senders = ServerDrawingEventSenders.registerTo(casting);
        var drawing = new ServerCastingDrawing(player, senders);

        casting.registerClientCasting(ClientCastingType.DRAWING, drawing::buildParameters);

        casting.bindInboundEvent(BeginGlyphC2SEvent.SPEC, drawing::beginGlyph);
        casting.bindInboundEvent(CancelGlyphC2SEvent.SPEC, drawing::cancelGlyph);
        casting.bindInboundEvent(DrawGlyphShapeC2SEvent.SPEC, drawing::drawGlyphShape);
        casting.bindInboundEvent(DrawGlyphStrokeC2SEvent.SPEC, drawing::drawGlyphStroke);

        casting.bindInboundEvent(PrepareSpellC2SEvent.SPEC, event -> drawing.preparingSpell = true);

        casting.registerTicker(drawing::tick);

        return casting.build();
    }

    private void beginGlyph(BeginGlyphC2SEvent event) {
        // TODO: input validation on glyphs?
        var glyph = new ServerDrawingGlyph(event.direction(), event.radius());

        this.glyphs.add(glyph);
        this.drawing = glyph;
        this.senders.drawGlyph(glyph);
    }

    private void cancelGlyph(CancelGlyphC2SEvent event) {
        var glyph = this.drawing;
        if (glyph != null) {
            this.drawing = null;
            this.glyphs.remove(glyph);
            this.senders.cancelDrawing();
        }
    }

    private void drawGlyphShape(DrawGlyphShapeC2SEvent event) {
        var glyph = this.drawing;
        if (glyph != null) {
            glyph.setShape(event.shape());

            if (glyph.tryForm(GlyphShapeStorage.get(this.player.server))) {
                this.senders.sendUpdateDrawing(glyph);
            }

            this.senders.broadcastUpdateDrawing(glyph);
        }
    }

    private void drawGlyphStroke(DrawGlyphStrokeC2SEvent event) {
        var glyph = this.drawing;
        if (glyph != null) {
            glyph.setStroke(event.node());
            this.senders.broadcastUpdateDrawing(glyph);
        }
    }

    @Nullable
    private ServerCasting.Factory tick() {
        if (this.preparingSpell) {
            var glyphTypes = this.glyphs.stream()
                    .map(ServerDrawingGlyph::getFormedType)
                    .filter(Objects::nonNull)
                    .toList();

            var cast = SpellCasting.cast(glyphTypes);
            if (cast != null) {
                var spell = this.buildSpellParameters();
                return (player, casting) -> cast.build(player, spell, casting);
            } else {
                return ServerCastingDrawing::build;
            }
        }

        return null;
    }

    private SpellParameters buildSpellParameters() {
        var glyphs = this.glyphs.stream()
                .map(ServerDrawingGlyph::asForm)
                .filter(Objects::nonNull)
                .toList();
        var direction = new Vec3f(this.player.getRotationVec(1.0F));

        return new SpellParameters(glyphs, direction);
    }

    private DrawingParameters buildParameters() {
        var glyphs = new ArrayList<DrawingGlyphParameters>(this.glyphs.size());
        for (var glyph : this.glyphs) {
            glyphs.add(glyph.asParameters());
        }
        return new DrawingParameters(glyphs);
    }
}
