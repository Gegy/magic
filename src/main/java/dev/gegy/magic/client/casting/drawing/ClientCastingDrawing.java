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
import dev.gegy.magic.client.casting.blend.CastingBlender;
import dev.gegy.magic.client.casting.drawing.input.DrawingCastingInput;
import dev.gegy.magic.client.effect.casting.drawing.DrawingEffect;
import dev.gegy.magic.client.effect.glyph.GlyphRenderParameters;
import dev.gegy.magic.client.effect.glyph.GlyphsEffect;
import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.SpellPrepareBlender;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransform;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.math.AnimationTimer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClientCastingDrawing {
    private static final int FADE_LENGTH = 10;

    private final Player player;
    private final ClientDrawingEventSenders senders;

    private final List<ClientDrawingGlyph> glyphs = new ArrayList<>();
    private ClientDrawingGlyph drawing;

    private final List<Fading> fading = new ArrayList<>();

    private final GlyphsEffect glyphsEffect = new DrawingGlyphsEffect();

    private ClientCastingDrawing(final Player player, final ClientDrawingEventSenders senders) {
        this.player = player;
        this.senders = senders;
    }

    public static ClientCasting build(final Player player, final DrawingParameters parameters, final ClientCastingBuilder casting) {
        final ClientDrawingEventSenders senders = ClientDrawingEventSenders.registerTo(casting);

        final ClientCastingDrawing drawing = new ClientCastingDrawing(player, senders);
        for (final DrawingGlyphParameters glyph : parameters.glyphs()) {
            drawing.drawGlyph(glyph);
        }

        casting.attachEffect(drawing.glyphsEffect);
        casting.attachEffect(new DrawingEffect(drawing));

        casting.bindInboundEvent(DrawGlyphS2CEvent.SPEC, drawing::drawGlyph);
        casting.bindInboundEvent(CancelDrawingS2CEvent.SPEC, drawing::cancelDrawing);
        casting.bindInboundEvent(UpdateDrawingS2CEvent.SPEC, drawing::updateDrawing);

        casting.registerTicker(drawing::tick);

        if (player.isLocalPlayer()) {
            drawing.bindInput(player, casting);
        }

        final CastingBlender.Entry<Spell, SpellTransformType> blendingSpell = casting.blendTo(CastingBlendType.SPELL);
        blendingSpell.blendInto(drawing::blendIntoPrepared);
        blendingSpell.blendOut(drawing::blendOut);

        return casting.build();
    }

    private void bindInput(final Player player, final ClientCastingBuilder casting) {
        final DrawingCastingInput input = new DrawingCastingInput();

        casting.bindInboundEvent(UpdateDrawingS2CEvent.SPEC, event -> {
            final GlyphType matchedType = event.formedGlyphType();
            if (matchedType != null) {
                input.finishDrawing(matchedType);
            }
        });

        casting.bindInboundEvent(CancelDrawingS2CEvent.SPEC, event -> input.cancelDrawing());

        casting.registerTicker(() -> {
            final ClientDrawingGlyph newDrawing = input.tick(this, player);
            updateOwnDrawing(newDrawing);
        });
    }

    private void updateOwnDrawing(final ClientDrawingGlyph drawing) {
        final ClientDrawingGlyph lastDrawing = this.drawing;
        this.drawing = drawing;

        if (lastDrawing != null && drawing == null) {
            fadeGlyph(lastDrawing);
        }
    }

    private void fadeGlyph(final ClientDrawingGlyph glyph) {
        final AnimationTimer timer = new AnimationTimer(FADE_LENGTH);
        fading.add(new Fading(glyph.toFading(timer), timer));
    }

    private void drawGlyph(final DrawGlyphS2CEvent event) {
        drawGlyph(event.glyph());
    }

    private void drawGlyph(final DrawingGlyphParameters parameters) {
        final ClientDrawingGlyph glyph = createGlyph(parameters);
        if (parameters.isFormed()) {
            glyphs.add(glyph);
        } else {
            drawing = glyph;
        }
    }

    private void cancelDrawing(final CancelDrawingS2CEvent event) {
        final ClientDrawingGlyph drawing = this.drawing;
        if (drawing != null) {
            this.drawing = null;
            fadeGlyph(drawing);
        }
    }

    private void updateDrawing(final UpdateDrawingS2CEvent event) {
        final ClientDrawingGlyph glyph = drawing;
        if (glyph != null) {
            glyph.setShape(event.shape());
            if (event.formedGlyphType() != null) {
                glyph.applyFormedType(event.formedGlyphType());
                glyphs.add(glyph);
                drawing = null;
            } else {
                glyph.applyStroke(event.stroke());
            }
        }
    }

    private void tick() {
        final ClientDrawingGlyph drawing = this.drawing;
        if (drawing != null) {
            drawing.tick();
        }

        for (final ClientDrawingGlyph glyph : glyphs) {
            glyph.tick();
        }

        fading.removeIf(Fading::tick);
    }

    private ClientDrawingGlyph createGlyph(final DrawingGlyphParameters parameters) {
        final GlyphPlane plane = new GlyphPlane(parameters.direction(), GlyphTransform.DRAW_DISTANCE);
        final ClientDrawingGlyph glyph = createGlyph(plane, parameters.radius(), parameters.shape());
        if (parameters.formedGlyphType() != null) {
            glyph.applyFormedType(parameters.formedGlyphType());
        }
        return glyph;
    }

    private ClientDrawingGlyph createGlyph(final GlyphPlane plane, final float radius, final GlyphShape shape) {
        final ClientDrawingGlyph glyph = new ClientDrawingGlyph(SpellSource.of(player), plane, radius);
        glyph.setShape(shape);
        return glyph;
    }

    @Nullable
    public ClientDrawingGlyph getDrawing() {
        return drawing;
    }

    public ClientDrawingEventSenders senders() {
        return senders;
    }

    private Spell blendIntoPrepared(final CastingBlendBuilder blend, final SpellTransformType transformType) {
        glyphs.sort(Comparator.comparingDouble(ClientDrawingGlyph::radius));

        final SpellSource source = SpellSource.of(player);
        final Vector3f direction = source.getLookVector(1.0f).toVector3f();

        final SpellTransform spellTransform = transformType.create(source, direction, glyphs.size());
        final Spell spell = Spell.prepare(source, spellTransform, glyphs);
        final SpellPrepareBlender blender = SpellPrepareBlender.create(glyphs, spell);

        blend.attachEffect(GlyphsEffect.fromSpell(blender.spell()));
        blend.registerTicker(blender::tick);

        return spell;
    }

    private void blendOut(final CastingBlendBuilder blend) {
        final AnimationTimer fadeTimer = new AnimationTimer(FADE_LENGTH);

        final List<FadingGlyph> fadingGlyphs = glyphs.stream()
                .map(glyph -> glyph.toFading(fadeTimer))
                .toList();

        blend.attachEffect(GlyphsEffect.fromFading(fadingGlyphs));
        blend.registerTicker(fadeTimer::tick);
    }

    private record Fading(FadingGlyph glyph, AnimationTimer timer) {
        public boolean tick() {
            return timer.tick();
        }
    }

    private final class DrawingGlyphsEffect implements GlyphsEffect {
        @Override
        public void render(final GlyphRenderParameters parameters, final WorldRenderContext context, final RenderFunction render) {
            final ClientDrawingGlyph drawing = ClientCastingDrawing.this.drawing;
            if (drawing != null) {
                parameters.setDrawing(drawing, context);
                render.accept(parameters);
            }

            for (final ClientDrawingGlyph glyph : glyphs) {
                parameters.setDrawing(glyph, context);
                render.accept(parameters);
            }

            for (final Fading fading : fading) {
                parameters.setFading(fading.glyph(), context);
                render.accept(parameters);
            }
        }
    }
}
