package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.network.codec.PacketCodec;

import java.util.Map;
import java.util.UUID;

public final record TeleportParameters(Map<UUID, TeleportTargetSymbol> targets) {
    public static final PacketCodec<TeleportParameters> CODEC = PacketCodec.mapOf(PacketCodec.UUID, TeleportTargetSymbol.CODEC)
            .map(TeleportParameters::new, TeleportParameters::targets);
}
