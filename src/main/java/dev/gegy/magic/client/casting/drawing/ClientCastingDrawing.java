package dev.gegy.magic.client.casting.drawing;

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
import dev.gegy.magic.client.effect.glyph.GlyphRenderParameters;
import dev.gegy.magic.client.effect.glyph.GlyphsEffect;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.SpellPrepareBlender;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.math.AnimationTimer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClientCastingDrawing {
    private static final int FADE_LENGTH = 10;

    private final PlayerEntity player;
    private final ClientDrawingEventSenders senders;

    private final List<ClientDrawingGlyph> glyphs = new ArrayList<>();
    private ClientDrawingGlyph drawing;

    private final List<Fading> fading = new ArrayList<>();

    private final GlyphsEffect glyphsEffect = new DrawingGlyphsEffect();

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

        casting.attachEffect(drawing.glyphsEffect);
        casting.attachEffect(new DrawingEffect(drawing));

        casting.bindInboundEvent(DrawGlyphS2CEvent.SPEC, drawing::drawGlyph);
        casting.bindInboundEvent(CancelDrawingS2CEvent.SPEC, drawing::cancelDrawing);
        casting.bindInboundEvent(UpdateDrawingS2CEvent.SPEC, drawing::updateDrawing);

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

        casting.bindInboundEvent(UpdateDrawingS2CEvent.SPEC, event -> {
            var matchedType = event.formedGlyphType();
            if (matchedType != null) {
                input.finishDrawing(matchedType);
            }
        });

        casting.bindInboundEvent(CancelDrawingS2CEvent.SPEC, event -> input.cancelDrawing());

        casting.registerTicker(() -> {
            var newDrawing = input.tick(this, player);
            this.updateOwnDrawing(newDrawing);
        });
    }

    private void updateOwnDrawing(ClientDrawingGlyph drawing) {
        var lastDrawing = this.drawing;
        this.drawing = drawing;

        if (lastDrawing != null && drawing == null) {
            this.fadeGlyph(lastDrawing);
        }
    }

    private void fadeGlyph(ClientDrawingGlyph glyph) {
        var timer = new AnimationTimer(FADE_LENGTH);
        this.fading.add(new Fading(glyph.toFading(timer), timer));
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
        var drawing = this.drawing;
        if (drawing != null) {
            this.drawing = null;
            this.fadeGlyph(drawing);
        }
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

        this.fading.removeIf(Fading::tick);
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

    private Spell blendIntoPrepared(CastingBlendBuilder blend, SpellTransformType transformType) {
        this.glyphs.sort(Comparator.comparingDouble(ClientDrawingGlyph::radius));

        var source = SpellSource.of(this.player);
        var direction = source.getLookVector(1.0F).toVector3f();

        var spellTransform = transformType.create(source, direction, this.glyphs.size());
        var spell = Spell.prepare(source, spellTransform, this.glyphs);
        var blender = SpellPrepareBlender.create(this.glyphs, spell);

        blend.attachEffect(GlyphsEffect.fromSpell(blender.spell()));
        blend.registerTicker(blender::tick);

        return spell;
    }

    private void blendOut(CastingBlendBuilder blend) {
        var fadeTimer = new AnimationTimer(FADE_LENGTH);

        var fadingGlyphs = this.glyphs.stream()
                .map(glyph -> glyph.toFading(fadeTimer))
                .toList();

        blend.attachEffect(GlyphsEffect.fromFading(fadingGlyphs));
        blend.registerTicker(fadeTimer::tick);
    }

    private static final record Fading(FadingGlyph glyph, AnimationTimer timer) {
        public boolean tick() {
            return this.timer.tick();
        }
    }

    private final class DrawingGlyphsEffect implements GlyphsEffect {
        @Override
        public void render(GlyphRenderParameters parameters, WorldRenderContext context, RenderFunction render) {
            var drawing = ClientCastingDrawing.this.drawing;
            if (drawing != null) {
                parameters.setDrawing(drawing, context);
                render.accept(parameters);
            }

            for (var glyph : ClientCastingDrawing.this.glyphs) {
                parameters.setDrawing(glyph, context);
                render.accept(parameters);
            }

            for (var fading : ClientCastingDrawing.this.fading) {
                parameters.setFading(fading.glyph(), context);
                render.accept(parameters);
            }
        }
    }
}
