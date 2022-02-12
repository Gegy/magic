package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.network.codec.PacketCodec;

public final record BeamParameters(boolean active) {
    public static final PacketCodec<BeamParameters> CODEC = PacketCodec.BOOLEAN.map(BeamParameters::new, BeamParameters::active);
}
