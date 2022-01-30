package dev.gegy.magic.casting;

import dev.gegy.magic.casting.drawing.ServerCastingDrawing;
import dev.gegy.magic.event.LateTrackingEvent;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
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

    private void onPlayerJoin(ServerPlayerEntity player) {
        var source = new ServerCastingSource(player);
        source.setCasting(ServerCastingDrawing::build);

        this.sources.put(player.getUuid(), source);
    }

    private void onPlayerLeave(ServerPlayerEntity player) {
        var source = this.sources.remove(player.getUuid());
        if (source != null) {
            source.close();
        }
    }

    public void handleEvent(ServerPlayerEntity player, Identifier id, PacketByteBuf buf) {
        var source = this.getSource(player);
        if (source != null) {
            source.handleEvent(id, buf);
        }
    }

    @Nullable
    private ServerCastingSource getSource(Entity entity) {
        return entity instanceof ServerPlayerEntity ? this.sources.get(entity.getUuid()) : null;
    }

    private void onServerStop(MinecraftServer server) {
        for (var source : this.sources.values()) {
            source.close();
        }
        this.sources.clear();
    }

    private void onServerTick(MinecraftServer server) {
        for (var source : this.sources.values()) {
            source.tick();
        }
    }

    private void onPlayerStartTracking(Entity trackedEntity, ServerPlayerEntity player) {
        var source = this.getSource(trackedEntity);
        if (source != null) {
            source.onStartTracking(player);
        }
    }

    private void onPlayerStopTracking(Entity trackedEntity, ServerPlayerEntity player) {
        var source = this.getSource(trackedEntity);
        if (source != null) {
            source.onStopTracking(player);
        }
    }
}
