package dev.gegy.magic.casting.drawing.event.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;

public final class CancelGlyphC2SEvent {
    public static final PacketCodec<CancelGlyphC2SEvent> CODEC = PacketCodec.unit(CancelGlyphC2SEvent::new);
    public static final CastingEventSpec<CancelGlyphC2SEvent> SPEC = CastingEventSpec.of(Magic.identifier("cancel_glyph"), CODEC);
}
