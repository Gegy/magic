package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.drawing.ServerCastingDrawing;
import dev.gegy.magic.client.casting.ClientCastingType;
import dev.gegy.magic.math.ColorRgb;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ServerCastingTeleport {
    private final ServerPlayerEntity player;
    private final List<TeleportTarget> targets;
    private boolean selectedTarget;

    private ServerCastingTeleport(ServerPlayerEntity player, List<TeleportTarget> targets) {
        this.player = player;
        this.targets = targets;
    }

    public static ServerCasting build(ServerPlayerEntity player, ServerCastingBuilder casting) {
        // TODO: hardcoded target
        var teleport = new ServerCastingTeleport(player, List.of(
                generateTarget(player, 'Y', Formatting.LIGHT_PURPLE),
                generateTarget(player, 'G', Formatting.GREEN),
                generateTarget(player, 'B', Formatting.BLUE),
                generateTarget(player, 'R', Formatting.RED),
                generateTarget(player, 'Y', Formatting.LIGHT_PURPLE),
                generateTarget(player, 'G', Formatting.GREEN),
                generateTarget(player, 'B', Formatting.BLUE),
                generateTarget(player, 'R', Formatting.RED),
                generateTarget(player, 'Y', Formatting.LIGHT_PURPLE),
                generateTarget(player, 'G', Formatting.GREEN),
                generateTarget(player, 'B', Formatting.BLUE),
                generateTarget(player, 'R', Formatting.RED),
                generateTarget(player, 'Y', Formatting.LIGHT_PURPLE),
                generateTarget(player, 'G', Formatting.GREEN),
                generateTarget(player, 'B', Formatting.BLUE),
                generateTarget(player, 'R', Formatting.RED),
                generateTarget(player, 'Y', Formatting.LIGHT_PURPLE),
                generateTarget(player, 'G', Formatting.GREEN),
                generateTarget(player, 'B', Formatting.BLUE),
                generateTarget(player, 'R', Formatting.RED)
        ));

        casting.registerClientCasting(ClientCastingType.TELEPORT, teleport::buildParameters);

        casting.bindInboundEvent(SelectTeleportTarget.SPEC, teleport::selectTarget);

        casting.registerTicker(teleport::tick);

        return casting.build();
    }

    private static TeleportTarget generateTarget(ServerPlayerEntity player, char character, Formatting formatting) {
        var symbol = new TeleportTargetSymbol(character, ColorRgb.of(Objects.requireNonNull(formatting.getColorValue(), "non-color formatting")));

        var world = player.getWorld();
        var dimension = world.getRegistryKey();
        var random = player.getRandom();
        var pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(
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
        if (this.player.isSneaking() || this.selectedTarget) {
            return ServerCastingDrawing::build;
        }

        return null;
    }

    private void teleportPlayer(TeleportTarget target) {
        var world = this.player.server.getWorld(target.dimension());
        var pos = Vec3d.ofBottomCenter(target.pos());
        this.player.teleport(world, pos.x, pos.y, pos.z, target.angle(), 0.0F);
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
        return new TeleportParameters(symbols);
    }
}
