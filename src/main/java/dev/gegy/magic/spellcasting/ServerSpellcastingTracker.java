package dev.gegy.magic.spellcasting;

import dev.gegy.magic.event.LateTrackingEvent;
import dev.gegy.magic.event.PlayerLeaveEvent;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.glyph.shape.GlyphNode;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class ServerSpellcastingTracker {
    public static final ServerSpellcastingTracker INSTANCE = new ServerSpellcastingTracker();

    private final Int2ObjectMap<ServerGlyph> glyphsById = new Int2ObjectOpenHashMap<>();

    private final Object2ObjectMap<UUID, ServerSpellcastingSource> sources = new Object2ObjectOpenHashMap<>();

    private int nextNetworkId;

    static {
        ServerLifecycleEvents.SERVER_STOPPING.register(INSTANCE::onServerStop);

        LateTrackingEvent.START.register(INSTANCE::onPlayerStartTracking);
        EntityTrackingEvents.STOP_TRACKING.register(INSTANCE::onPlayerStopTracking);

        PlayerLeaveEvent.EVENT.register(INSTANCE::onPlayerLeave);
    }

    private ServerSpellcastingTracker() {
    }

    @Nullable
    public ServerGlyph startDrawing(ServerPlayerEntity player, Vec3f direction, float radius) {
        ServerSpellcastingSource source = this.getOrCreateSource(player);
        if (source.canDraw()) {
            ServerGlyph glyph = this.addGlyph(source, direction, radius);
            source.startDrawing(glyph);
            return glyph;
        }

        return null;
    }

    public ServerGlyph addGlyph(ServerPlayerEntity player, Vec3f direction, float radius) {
        ServerSpellcastingSource source = this.getOrCreateSource(player);
        return this.addGlyph(source, direction, radius);
    }

    private ServerGlyph addGlyph(ServerSpellcastingSource source, Vec3f direction, float radius) {
        int networkId = this.nextNetworkId++;
        ServerGlyph glyph = new ServerGlyph(networkId, source, direction, radius);

        this.glyphsById.put(networkId, glyph);
        source.addGlyph(glyph);

        return glyph;
    }

    @Nullable
    public ServerGlyph removeGlyph(int networkId) {
        ServerGlyph glyph = this.glyphsById.remove(networkId);
        if (glyph != null) {
            ServerSpellcastingSource source = glyph.getSource();
            source.removeGlyph(glyph);
            return glyph;
        } else {
            return null;
        }
    }

    public void updateDrawingShape(ServerPlayerEntity player, int shape) {
        ServerSpellcastingSource source = this.getSource(player);
        if (source != null) {
            source.updateDrawingShape(shape);
        }
    }

    public void updateDrawingStroke(ServerPlayerEntity player, @Nullable GlyphNode node) {
        ServerSpellcastingSource source = this.getSource(player);
        if (source != null) {
            source.updateDrawingStroke(node);
        }
    }

    public void cancelDrawingGlyph(ServerPlayerEntity player) {
        ServerSpellcastingSource source = this.getSource(player);
        if (source != null) {
            ServerGlyph glyph = source.stopDrawing();
            if (glyph != null) {
                this.removeGlyph(glyph.getNetworkId());
            }
        }
    }

    public void prepareSpell(ServerPlayerEntity player) {
        ServerSpellcastingSource source = this.getSource(player);
        if (source != null) {
            source.prepareSpell();
        }
    }

    public void cancelSpell(ServerPlayerEntity player) {
        ServerSpellcastingSource source = this.getSource(player);
        if (source != null) {
            source.cancelSpell();
        }
    }

    private ServerSpellcastingSource getOrCreateSource(ServerPlayerEntity player) {
        return this.sources.computeIfAbsent(player.getUuid(), u -> new ServerSpellcastingSource(player));
    }

    @Nullable
    private ServerSpellcastingSource getSource(Entity entity) {
        if (entity instanceof ServerPlayerEntity) {
            return this.sources.get(entity.getUuid());
        }
        return null;
    }

    private void onServerStop(MinecraftServer server) {
        this.glyphsById.clear();
        this.sources.clear();
        this.nextNetworkId = 0;
    }

    private void onPlayerStartTracking(Entity trackedEntity, ServerPlayerEntity player) {
        ServerSpellcastingSource source = this.getSource(trackedEntity);
        if (source != null) {
            source.onStartTracking(player);
        }
    }

    private void onPlayerStopTracking(Entity trackedEntity, ServerPlayerEntity player) {
        ServerSpellcastingSource source = this.getSource(trackedEntity);
        if (source != null) {
            source.onStopTracking(player);
        }
    }

    private void onPlayerLeave(ServerPlayerEntity player) {
        ServerSpellcastingSource source = this.sources.remove(player.getUuid());
        if (source != null) {
            List<ServerGlyph> glyphs = source.clearGlyphs();
            for (ServerGlyph glyph : glyphs) {
                this.glyphsById.remove(glyph.getNetworkId());
            }
        }
    }
}
