package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.ServerCastingDrawing;
import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.client.casting.ClientCastingType;
import dev.gegy.magic.math.ColorRgb;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ServerCastingTeleport {
    private final ServerPlayer player;
    private final SpellParameters spell;
    private final List<TeleportTarget> targets;
    private boolean selectedTarget;

    private ServerCastingTeleport(final ServerPlayer player, final SpellParameters spell, final List<TeleportTarget> targets) {
        this.player = player;
        this.spell = spell;
        this.targets = targets;
    }

    public static ServerCasting build(final ServerPlayer player, final SpellParameters spell, final ServerCastingBuilder casting) {
        // TODO: hardcoded target
        final ServerCastingTeleport teleport = new ServerCastingTeleport(player, spell, List.of(
                generateTarget(player, 'Y', ChatFormatting.LIGHT_PURPLE),
                generateTarget(player, 'G', ChatFormatting.GREEN),
                generateTarget(player, 'B', ChatFormatting.BLUE),
                generateTarget(player, 'R', ChatFormatting.RED),
                generateTarget(player, 'Y', ChatFormatting.LIGHT_PURPLE),
                generateTarget(player, 'G', ChatFormatting.GREEN),
                generateTarget(player, 'B', ChatFormatting.BLUE),
                generateTarget(player, 'R', ChatFormatting.RED),
                generateTarget(player, 'Y', ChatFormatting.LIGHT_PURPLE),
                generateTarget(player, 'G', ChatFormatting.GREEN),
                generateTarget(player, 'B', ChatFormatting.BLUE),
                generateTarget(player, 'R', ChatFormatting.RED),
                generateTarget(player, 'Y', ChatFormatting.LIGHT_PURPLE),
                generateTarget(player, 'G', ChatFormatting.GREEN),
                generateTarget(player, 'B', ChatFormatting.BLUE),
                generateTarget(player, 'R', ChatFormatting.RED),
                generateTarget(player, 'Y', ChatFormatting.LIGHT_PURPLE),
                generateTarget(player, 'G', ChatFormatting.GREEN),
                generateTarget(player, 'B', ChatFormatting.BLUE),
                generateTarget(player, 'R', ChatFormatting.RED)
        ));

        casting.registerClientCasting(ClientCastingType.TELEPORT, teleport::buildParameters);

        casting.bindInboundEvent(SelectTeleportTarget.SPEC, teleport::selectTarget);

        casting.registerTicker(teleport::tick);

        return casting.build();
    }

    private static TeleportTarget generateTarget(final ServerPlayer player, final char character, final ChatFormatting formatting) {
        final TeleportTargetSymbol symbol = new TeleportTargetSymbol(character, ColorRgb.of(Objects.requireNonNull(formatting.getColor(), "non-color formatting")));

        final ServerLevel level = player.serverLevel();
        final ResourceKey<Level> dimension = level.dimension();
        final RandomSource random = player.getRandom();
        final BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(
                random.nextInt(16) - random.nextInt(16),
                0,
                random.nextInt(16) - random.nextInt(16)
        ));

        return TeleportTarget.create(symbol, dimension, pos, 0.0f);
    }

    private void selectTarget(final SelectTeleportTarget event) {
        final TeleportTarget target = lookupTarget(event.targetId());
        selectedTarget = true;

        if (target != null) {
            teleportPlayer(target);
        }
    }

    @Nullable
    private ServerCasting.Factory tick() {
        // TODO: proper cancel input
        if (player.isShiftKeyDown() || selectedTarget) {
            return ServerCastingDrawing::build;
        }

        return null;
    }

    private void teleportPlayer(final TeleportTarget target) {
        final ServerLevel level = player.server.getLevel(target.dimension());
        final Vec3 pos = Vec3.atBottomCenterOf(target.pos());
        player.teleportTo(level, pos.x, pos.y, pos.z, target.angle(), 0.0f);
    }

    @Nullable
    private TeleportTarget lookupTarget(final UUID id) {
        for (final TeleportTarget target : targets) {
            if (target.id().equals(id)) {
                return target;
            }
        }
        return null;
    }

    private TeleportParameters buildParameters() {
        final Object2ObjectOpenHashMap<UUID, TeleportTargetSymbol> symbols = new Object2ObjectOpenHashMap<UUID, TeleportTargetSymbol>();
        for (final TeleportTarget target : targets) {
            symbols.put(target.id(), target.symbol());
        }
        return new TeleportParameters(spell, symbols);
    }
}
