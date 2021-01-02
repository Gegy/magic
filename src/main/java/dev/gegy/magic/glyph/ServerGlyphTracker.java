package dev.gegy.magic.glyph;

import com.google.common.collect.ImmutableList;
import dev.gegy.magic.network.s2c.CreateGlyphS2CPacket;
import dev.gegy.magic.network.s2c.FinishGlyphS2CPacket;
import dev.gegy.magic.network.s2c.RemoveGlyphS2CPacket;
import dev.gegy.magic.network.s2c.UpdateGlyphS2CPacket;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ServerGlyphTracker {
    public static final ServerGlyphTracker INSTANCE = new ServerGlyphTracker();

    private final Int2ObjectMap<ServerGlyph> glyphsById = new Int2ObjectOpenHashMap<>();
    private final Object2ObjectMap<UUID, List<ServerGlyph>> glyphsBySource = new Object2ObjectOpenHashMap<>();

    private final Object2ObjectMap<UUID, ServerGlyph> drawingBySource = new Object2ObjectOpenHashMap<>();

    private int nextNetworkId;

    static {
        EntityTrackingEvents.START_TRACKING.register(INSTANCE::onPlayerStartTracking);
        EntityTrackingEvents.STOP_TRACKING.register(INSTANCE::onPlayerStopTracking);
    }

    private ServerGlyphTracker() {
    }

    public ServerGlyph startDrawingGlyph(ServerPlayerEntity source, GlyphPlane plane, float radius) {
        ServerGlyph glyph = this.addGlyph(source, plane, radius);
        this.drawingBySource.put(source.getUuid(), glyph);

        return glyph;
    }

    public ServerGlyph addGlyph(ServerPlayerEntity source, GlyphPlane plane, float radius) {
        int networkId = this.nextNetworkId++;

        ServerGlyph glyph = new ServerGlyph(networkId, source, plane, radius);
        this.glyphsById.put(networkId, glyph);
        this.glyphsBySource.computeIfAbsent(source.getUuid(), u -> new ArrayList<>()).add(glyph);

        this.sendGlyphCreateToTracking(glyph);

        return glyph;
    }

    @Nullable
    public ServerGlyph removeGlyph(int networkId) {
        ServerGlyph glyph = this.glyphsById.remove(networkId);
        if (glyph == null) {
            return null;
        }

        UUID sourceUuid = glyph.getSource().getUuid();
        List<ServerGlyph> glyphsBySource = this.glyphsBySource.get(sourceUuid);
        if (glyphsBySource != null && glyphsBySource.remove(glyph)) {
            if (glyphsBySource.isEmpty()) {
                this.glyphsBySource.remove(sourceUuid);
            }
        }

        this.sendGlyphRemoveToTracking(glyph);

        return glyph;
    }

    public void updateDrawing(ServerPlayerEntity source, int shape) {
        ServerGlyph glyph = this.drawingBySource.get(source.getUuid());
        if (glyph != null) {
            glyph.setShape(shape);
            if (glyph.tryMatchSpell()) {
                PacketByteBuf packet = FinishGlyphS2CPacket.create(glyph);
                FinishGlyphS2CPacket.sendTo(source, packet);
            }

            this.sendGlyphUpdateToTracking(glyph);
        }
    }

    private void sendGlyphUpdateToTracking(ServerGlyph glyph) {
        PacketByteBuf packet = UpdateGlyphS2CPacket.create(glyph);
        for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(glyph.getSource())) {
            UpdateGlyphS2CPacket.sendTo(trackingPlayer, packet);
        }
    }

    private void sendGlyphCreateToTracking(ServerGlyph glyph) {
        PacketByteBuf packet = CreateGlyphS2CPacket.create(glyph);
        for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(glyph.getSource())) {
            CreateGlyphS2CPacket.sendTo(trackingPlayer, packet);
        }
    }

    private void sendGlyphRemoveToTracking(ServerGlyph glyph) {
        PacketByteBuf packet = RemoveGlyphS2CPacket.create(glyph.getNetworkId());
        for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(glyph.getSource())) {
            RemoveGlyphS2CPacket.sendTo(trackingPlayer, packet);
        }
    }

    private void onPlayerStartTracking(Entity trackedEntity, ServerPlayerEntity player) {
        List<ServerGlyph> glyphs = this.getGlyphsForSource(trackedEntity);
        for (ServerGlyph glyph : glyphs) {
            PacketByteBuf packet = CreateGlyphS2CPacket.create(glyph);
            CreateGlyphS2CPacket.sendTo(player, packet);
        }
    }

    private void onPlayerStopTracking(Entity trackedEntity, ServerPlayerEntity player) {
        List<ServerGlyph> glyphs = this.getGlyphsForSource(trackedEntity);
        for (ServerGlyph glyph : glyphs) {
            PacketByteBuf packet = RemoveGlyphS2CPacket.create(glyph.getNetworkId());
            RemoveGlyphS2CPacket.sendTo(player, packet);
        }
    }

    private List<ServerGlyph> getGlyphsForSource(Entity source) {
        if (source instanceof ServerPlayerEntity) {
            return this.glyphsBySource.getOrDefault(source.getUuid(), ImmutableList.of());
        }
        return ImmutableList.of();
    }
}
