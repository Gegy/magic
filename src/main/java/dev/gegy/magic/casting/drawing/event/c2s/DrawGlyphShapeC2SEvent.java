package dev.gegy.magic.casting.drawing.event.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.network.codec.PacketCodec;

public record DrawGlyphShapeC2SEvent(GlyphShape shape) {
    public static final PacketCodec<DrawGlyphShapeC2SEvent> CODEC = GlyphShape.PACKET_CODEC.map(DrawGlyphShapeC2SEvent::new, DrawGlyphShapeC2SEvent::shape);
    public static final CastingEventSpec<DrawGlyphShapeC2SEvent> SPEC = CastingEventSpec.of(Magic.identifier("glyph_shape"), CODEC);
}
