package dev.gegy.magic.casting.drawing.event.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;

public final class PrepareSpellC2SEvent {
    public static final PacketCodec<PrepareSpellC2SEvent> CODEC = PacketCodec.unit(PrepareSpellC2SEvent::new);
    public static final CastingEventSpec<PrepareSpellC2SEvent> SPEC = CastingEventSpec.of(Magic.identifier("prepare_spell"), CODEC);
}
