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
import net.minecraft.server.level.ServerPlayer;
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

    private ServerCastingTeleport(ServerPlayer player, SpellParameters spell, List<TeleportTarget> targets) {
        this.player = player;
        this.spell = spell;
        this.targets = targets;
    }

    public static ServerCasting build(ServerPlayer player, SpellParameters spell, ServerCastingBuilder casting) {
        // TODO: hardcoded target
        var teleport = new ServerCastingTeleport(player, spell, List.of(
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

    private static TeleportTarget generateTarget(ServerPlayer player, char character, ChatFormatting formatting) {
        var symbol = new TeleportTargetSymbol(character, ColorRgb.of(Objects.requireNonNull(formatting.getColor(), "non-color formatting")));

        var level = player.getLevel();
        var dimension = level.dimension();
        var random = player.getRandom();
        var pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(
                random.nextInt(16) - random.nextInt(16),
                0,
                random.nextInt(16) - random.nextInt(16)
        ));

        return TeleportTarget.create(symbol, dimension, pos, 0.0F);
    }

    private void selectTarget(SelectTeleportTarget event) {
        var target = this.lookupTarget(event.targetId());
        this.selectedTarget = true;

        if (target != null) {
            this.teleportPlayer(target);
        }
    }

    @Nullable
    private ServerCasting.Factory tick() {
        // TODO: proper cancel input
        if (this.player.isShiftKeyDown() || this.selectedTarget) {
            return ServerCastingDrawing::build;
        }

        return null;
    }

    private void teleportPlayer(TeleportTarget target) {
        var level = this.player.server.getLevel(target.dimension());
        var pos = Vec3.atBottomCenterOf(target.pos());
        this.player.teleportTo(level, pos.x, pos.y, pos.z, target.angle(), 0.0F);
    }

    @Nullable
    private TeleportTarget lookupTarget(UUID id) {
        for (var target : this.targets) {
            if (target.id().equals(id)) {
                return target;
            }
        }
        return null;
    }

    private TeleportParameters buildParameters() {
        var symbols = new Object2ObjectOpenHashMap<UUID, TeleportTargetSymbol>();
        for (var target : this.targets) {
            symbols.put(target.id(), target.symbol());
        }
        return new TeleportParameters(this.spell, symbols);
    }
}
