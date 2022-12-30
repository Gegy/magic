package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;

import java.util.UUID;

public record SelectTeleportTarget(UUID targetId) {
    public static final PacketCodec<SelectTeleportTarget> CODEC = PacketCodec.UUID
            .map(SelectTeleportTarget::new, SelectTeleportTarget::targetId);

    public static final CastingEventSpec<SelectTeleportTarget> SPEC = CastingEventSpec.of(Magic.identifier("select_target"), CODEC);
}
