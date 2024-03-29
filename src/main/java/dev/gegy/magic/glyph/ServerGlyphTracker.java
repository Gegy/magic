package dev.gegy.magic.glyph;

import dev.gegy.magic.event.LateTrackingEvent;
import dev.gegy.magic.event.PlayerLeaveEvent;
import dev.gegy.magic.glyph.shape.GlyphNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.util.math.Vec3f;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ServerGlyphTracker {
    public static final ServerGlyphTracker INSTANCE = new ServerGlyphTracker();

    private final Int2ObjectMap<ServerGlyph> glyphsById = new Int2ObjectOpenHashMap<>();

    private final Object2ObjectMap<UUID, ServerGlyphSource> glyphSources = new Object2ObjectOpenHashMap<>();

    private int nextNetworkId;

    static {
        ServerLifecycleEvents.SERVER_STOPPING.register(INSTANCE::onServerStop);

        LateTrackingEvent.START.register(INSTANCE::onPlayerStartTracking);
        EntityTrackingEvents.STOP_TRACKING.register(INSTANCE::onPlayerStopTracking);

        PlayerLeaveEvent.EVENT.register(INSTANCE::onPlayerLeave);
    }

    private ServerGlyphTracker() {
    }

    @Nullable
    public ServerGlyph startDrawing(ServerPlayerEntity player, Vec3f direction, float radius) {
        ServerGlyphSource source = this.getOrCreateSource(player);
        if (source.canDraw()) {
            ServerGlyph glyph = this.addGlyph(source, direction, radius);
            source.startDrawing(glyph);
            return glyph;
        }

        return null;
    }

    public ServerGlyph addGlyph(ServerPlayerEntity player, Vec3f direction, float radius) {
        ServerGlyphSource source = this.getOrCreateSource(player);
        return this.addGlyph(source, direction, radius);
    }

    private ServerGlyph addGlyph(ServerGlyphSource source, Vec3f direction, float radius) {
        int networkId = this.nextNetworkId++;
        ServerGlyph glyph = new ServerGlyph(networkId, source, direction, radius);

        this.glyphsById.put(networkId, glyph);
        source.addGlyph(glyph);

        return glyph;
    }

    @Nullable
    public ServerGlyph removeGlyph(int networkId) {
        ServerGlyph glyph = this.glyphsById.remove(networkId);
        if (glyph == null) {
            return null;
        }

        ServerGlyphSource source = glyph.getSource();
        if (source.removeGlyph(glyph) && source.isEmpty()) {
            this.glyphSources.remove(source.getId());
        }

        return glyph;
    }

    public void updateDrawingShape(ServerPlayerEntity player, int shape) {
        ServerGlyphSource source = this.getSource(player);
        if (source != null) {
            source.updateDrawingShape(shape);
        }
    }

    public void updateDrawingStroke(ServerPlayerEntity player, @Nullable GlyphNode node) {
        ServerGlyphSource source = this.getSource(player);
        if (source != null) {
            source.updateDrawingStroke(node);
        }
    }

    public void cancelDrawingGlyph(ServerPlayerEntity player) {
        ServerGlyphSource source = this.getSource(player);
        if (source != null) {
            ServerGlyph glyph = source.stopDrawing();
            if (glyph != null) {
                this.removeGlyph(glyph.getNetworkId());
            }
        }
    }

    public void prepareSpell(ServerPlayerEntity player) {
        ServerGlyphSource source = this.getSource(player);
        if (source != null) {
            source.prepareSpell();
        }
    }

    public void cancelSpell(ServerPlayerEntity player) {
        ServerGlyphSource source = this.getSource(player);
        if (source != null) {
            source.cancelSpell();
        }
    }

    private ServerGlyphSource getOrCreateSource(ServerPlayerEntity player) {
        return this.glyphSources.computeIfAbsent(player.getUuid(), u -> new ServerGlyphSource(player));
    }

    @Nullable
    private ServerGlyphSource getSource(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            return this.glyphSources.get(entity.getUuid());
        }
        return null;
    }

    private void onServerStop(MinecraftServer server) {
        this.glyphsById.clear();
        this.glyphSources.clear();
        this.nextNetworkId = 0;
    }

    private void onPlayerStartTracking(Entity trackedEntity, ServerPlayerEntity player) {
        ServerGlyphSource source = this.getSource(trackedEntity);
        if (source != null) {
            source.onStartTracking(player);
        }
    }

    private void onPlayerStopTracking(Entity trackedEntity, ServerPlayerEntity player) {
        ServerGlyphSource source = this.getSource(trackedEntity);
        if (source != null) {
            source.onStopTracking(player);
        }
    }

    private void onPlayerLeave(ServerPlayerEntity player) {
        ServerGlyphSource source = this.glyphSources.remove(player.getUuid());
        if (source != null) {
            source.onRemove();
        }
    }
}
