package dev.gegy.magic.casting.spell.teleport;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final record TeleportTarget(
        UUID id, TeleportTargetSymbol symbol,
        ResourceKey<Level> dimension, BlockPos pos, float angle
) {
    public static TeleportTarget create(TeleportTargetSymbol symbol, ResourceKey<Level> dimension, BlockPos pos, float angle) {
        return new TeleportTarget(UUID.randomUUID(), symbol, dimension, pos, angle);
    }
}
