package dev.gegy.magic.casting.spell.teleport;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.util.UUID;

public final record TeleportTarget(
        UUID id, TeleportTargetSymbol symbol,
        RegistryKey<World> dimension, BlockPos pos, float angle
) {
    public static TeleportTarget create(TeleportTargetSymbol symbol, RegistryKey<World> dimension, BlockPos pos, float angle) {
        return new TeleportTarget(UUID.randomUUID(), symbol, dimension, pos, angle);
    }
}
