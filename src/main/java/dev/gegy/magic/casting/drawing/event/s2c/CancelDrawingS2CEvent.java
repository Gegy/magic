package dev.gegy.magic.casting.drawing.event.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;

public final record CancelDrawingS2CEvent() {
    public static final PacketCodec<CancelDrawingS2CEvent> CODEC = PacketCodec.unit(CancelDrawingS2CEvent::new);
    public static final CastingEventSpec<CancelDrawingS2CEvent> SPEC = CastingEventSpec.of(Magic.identifier("cancel_drawing"), CODEC);
}
