package dev.gegy.magic.client.casting.drawing;

import com.google.common.collect.Iterators;
import dev.gegy.magic.casting.drawing.DrawingGlyphParameters;
import dev.gegy.magic.casting.drawing.DrawingParameters;
import dev.gegy.magic.casting.drawing.event.ClientDrawingEventSenders;
import dev.gegy.magic.casting.drawing.event.s2c.CancelDrawingS2CEvent;
import dev.gegy.magic.casting.drawing.event.s2c.DrawGlyphS2CEvent;
import dev.gegy.magic.casting.drawing.event.s2c.UpdateDrawingS2CEvent;
import dev.gegy.magic.client.casting.ClientCasting;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.casting.blend.CastingBlendBuilder;
import dev.gegy.magic.client.casting.blend.CastingBlendType;
import dev.gegy.magic.client.casting.drawing.input.DrawingCastingInput;
import dev.gegy.magic.client.effect.casting.drawing.DrawingEffect;
import dev.gegy.magic.client.effect.glyph.GlyphEffect;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.SpellPrepareBlender;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.math.AnimationTimer;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public final class ClientCastingDrawing {
    private final PlayerEntity player;
    private final ClientDrawingEventSenders senders;

    private final List<ClientDrawingGlyph> glyphs = new ArrayList<>();
    private ClientDrawingGlyph drawing;

    private final AllGlyphs allGlyphs = new AllGlyphs();

    private ClientCastingDrawing(PlayerEntity player, ClientDrawingEventSenders senders) {
        this.player = player;
        this.senders = senders;
    }

    public static ClientCasting build(PlayerEntity player, DrawingParameters parameters, ClientCastingBuilder casting) {
        var senders = ClientDrawingEventSenders.registerTo(casting);

        var drawing = new ClientCastingDrawing(player, senders);
        for (var glyph : parameters.glyphs()) {
            drawing.drawGlyph(glyph);
        }

        casting.attachEffect(GlyphEffect.drawing(drawing.allGlyphs));
        casting.attachEffect(new DrawingEffect(drawing));

        casting.registerInboundEvent(DrawGlyphS2CEvent.SPEC, drawing::drawGlyph);
        casting.registerInboundEvent(CancelDrawingS2CEvent.SPEC, drawing::cancelDrawing);
        casting.registerInboundEvent(UpdateDrawingS2CEvent.SPEC, drawing::updateDrawing);

        casting.registerTicker(drawing::tick);

        if (player.isMainPlayer()) {
            drawing.bindInput(player, casting);
        }

        var blendingSpell = casting.blendTo(CastingBlendType.SPELL);
        blendingSpell.blendInto(drawing::blendIntoPrepared);
        blendingSpell.blendOut(drawing::blendOut);

        return casting.build();
    }

    private void bindInput(PlayerEntity player, ClientCastingBuilder casting) {
        var input = new DrawingCastingInput();

        casting.registerInboundEvent(UpdateDrawingS2CEvent.SPEC, event -> {
            var matchedType = event.formedGlyphType();
            if (matchedType != null) {
                input.finishDrawing(matchedType);
            }
        });

        casting.registerInboundEvent(CancelDrawingS2CEvent.SPEC, event -> {
            input.cancelDrawing();
        });

        casting.registerTicker(() -> {
            this.drawing = input.tick(this, player);
        });
    }

    private void drawGlyph(DrawGlyphS2CEvent event) {
        this.drawGlyph(event.glyph());
    }

    private void drawGlyph(DrawingGlyphParameters parameters) {
        var glyph = this.createGlyph(parameters);
        if (parameters.isFormed()) {
            this.glyphs.add(glyph);
        } else {
            this.drawing = glyph;
        }
    }

    private void cancelDrawing(CancelDrawingS2CEvent event) {
        this.drawing = null;
    }

    private void updateDrawing(UpdateDrawingS2CEvent event) {
        var glyph = this.drawing;
        if (glyph != null) {
            glyph.setShape(event.shape());
            if (event.formedGlyphType() != null) {
                glyph.applyFormedType(event.formedGlyphType());
                this.glyphs.add(glyph);
                this.drawing = null;
            } else {
                glyph.applyStroke(event.stroke());
            }
        }
    }

    private void tick() {
        var drawing = this.drawing;
        if (drawing != null) {
            drawing.tick();
        }

        for (var glyph : this.glyphs) {
            glyph.tick();
        }
    }

    private ClientDrawingGlyph createGlyph(DrawingGlyphParameters parameters) {
        var plane = new GlyphPlane(parameters.direction(), GlyphTransform.DRAW_DISTANCE);
        var glyph = this.createGlyph(plane, parameters.radius(), parameters.shape());
        if (parameters.formedGlyphType() != null) {
            glyph.applyFormedType(parameters.formedGlyphType());
        }
        return glyph;
    }

    private ClientDrawingGlyph createGlyph(GlyphPlane plane, float radius, int shape) {
        var glyph = new ClientDrawingGlyph(SpellSource.of(this.player), plane, radius);
        glyph.setShape(shape);
        return glyph;
    }

    @Nullable
    public ClientDrawingGlyph getDrawing() {
        return this.drawing;
    }

    public ClientDrawingEventSenders senders() {
        return this.senders;
    }

    private Spell blendIntoPrepared(CastingBlendBuilder blend) {
        var spell = Spell.prepare(SpellSource.of(this.player), this.glyphs);

        this.glyphs.sort(Comparator.comparingDouble(ClientDrawingGlyph::radius));

        var prepareSpell = SpellPrepareBlender.create(this.glyphs, spell);

        blend.attachEffect(GlyphEffect.spell(prepareSpell.spell()));
        blend.registerTicker(prepareSpell::tick);

        return spell;
    }

    private void blendOut(CastingBlendBuilder blend) {
        var fadeTimer = new AnimationTimer(10);

        var fadingGlyphs = this.glyphs.stream()
                .map(glyph -> new FadingGlyph(glyph.source(), glyph.plane(), glyph.asForm(), fadeTimer))
                .collect(Collectors.toList());

        blend.attachEffect(GlyphEffect.fading(fadingGlyphs));
        blend.registerTicker(fadeTimer::tick);
    }

    private final class AllGlyphs extends AbstractCollection<ClientDrawingGlyph> {
        @Override
        public Iterator<ClientDrawingGlyph> iterator() {
            ClientDrawingGlyph drawingGlyph = ClientCastingDrawing.this.drawing;
            if (drawingGlyph != null) {
                return Iterators.concat(
                        Iterators.singletonIterator(drawingGlyph),
                        ClientCastingDrawing.this.glyphs.iterator()
                );
            } else {
                return ClientCastingDrawing.this.glyphs.iterator();
            }
        }

        @Override
        public int size() {
            ClientDrawingGlyph drawingGlyph = ClientCastingDrawing.this.drawing;
            if (drawingGlyph != null) {
                return ClientCastingDrawing.this.glyphs.size() + 1;
            } else {
                return ClientCastingDrawing.this.glyphs.size();
            }
        }
    }
}
