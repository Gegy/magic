package dev.gegy.magic.casting.drawing.event.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.drawing.DrawingGlyphParameters;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;

public record DrawGlyphS2CEvent(DrawingGlyphParameters glyph) {
    public static final PacketCodec<DrawGlyphS2CEvent> CODEC = DrawingGlyphParameters.CODEC.map(DrawGlyphS2CEvent::new, DrawGlyphS2CEvent::glyph);
    public static final CastingEventSpec<DrawGlyphS2CEvent> SPEC = CastingEventSpec.of(Magic.identifier("draw_glyph"), CODEC);
}
