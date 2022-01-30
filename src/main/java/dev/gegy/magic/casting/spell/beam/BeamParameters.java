package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.network.codec.PacketCodec;

public final record BeamParameters() {
    public static final PacketCodec<BeamParameters> CODEC = PacketCodec.unit(BeamParameters::new);
}
