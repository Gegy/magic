package dev.gegy.magic.casting;

import dev.gegy.magic.casting.drawing.ServerCastingDrawing;
import dev.gegy.magic.event.LateTrackingEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ServerCastingTracker {
    public static final ServerCastingTracker INSTANCE = new ServerCastingTracker();

    private final Object2ObjectMap<UUID, ServerCastingSource> sources = new Object2ObjectOpenHashMap<>();

    private ServerCastingTracker() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> INSTANCE.onPlayerJoin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> INSTANCE.onPlayerLeave(handler.player));

        ServerTickEvents.END_SERVER_TICK.register(INSTANCE::onServerTick);
        ServerLifecycleEvents.SERVER_STOPPING.register(INSTANCE::onServerStop);

        LateTrackingEvent.START.register(INSTANCE::onPlayerStartTracking);
        EntityTrackingEvents.STOP_TRACKING.register(INSTANCE::onPlayerStopTracking);
    }

    private void onPlayerJoin(final ServerPlayer player) {
        final ServerCastingSource source = new ServerCastingSource(player);
        source.setCasting(ServerCastingDrawing::build);

        sources.put(player.getUUID(), source);
    }

    private void onPlayerLeave(final ServerPlayer player) {
        final ServerCastingSource source = sources.remove(player.getUUID());
        if (source != null) {
            source.close();
        }
    }

    public void handleEvent(final ServerPlayer player, final ResourceLocation id, final FriendlyByteBuf buf) {
        final ServerCastingSource source = getSource(player);
        if (source != null) {
            source.handleEvent(id, buf);
        }
    }

    @Nullable
    private ServerCastingSource getSource(final Entity entity) {
        return entity instanceof ServerPlayer ? sources.get(entity.getUUID()) : null;
    }

    private void onServerStop(final MinecraftServer server) {
        for (final ServerCastingSource source : sources.values()) {
            source.close();
        }
        sources.clear();
    }

    private void onServerTick(final MinecraftServer server) {
        for (final ServerCastingSource source : sources.values()) {
            source.tick();
        }
    }

    private void onPlayerStartTracking(final Entity trackedEntity, final ServerPlayer player) {
        final ServerCastingSource source = getSource(trackedEntity);
        if (source != null) {
            source.onStartTracking(player);
        }
    }

    private void onPlayerStopTracking(final Entity trackedEntity, final ServerPlayer player) {
        final ServerCastingSource source = getSource(trackedEntity);
        if (source != null) {
            source.onStopTracking(player);
        }
    }
}
