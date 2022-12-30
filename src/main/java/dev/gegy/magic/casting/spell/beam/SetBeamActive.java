package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;

public record SetBeamActive(boolean active) {
    public static final PacketCodec<SetBeamActive> CODEC = PacketCodec.BOOLEAN.map(SetBeamActive::new, SetBeamActive::active);
    public static final CastingEventSpec<SetBeamActive> SPEC = CastingEventSpec.of(Magic.identifier("set_active"), CODEC);
}
