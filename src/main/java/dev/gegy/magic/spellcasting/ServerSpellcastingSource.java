package dev.gegy.magic.spellcasting;

import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.glyph.shape.GlyphShapeStorage;
import dev.gegy.magic.network.s2c.CreateGlyphS2CPacket;
import dev.gegy.magic.network.s2c.FinishGlyphS2CPacket;
import dev.gegy.magic.network.s2c.RemoveGlyphS2CPacket;
import dev.gegy.magic.network.s2c.SetPreparedSpellS2CPacket;
import dev.gegy.magic.network.s2c.UpdateGlyphS2CPacket;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ServerSpellcastingSource {
    private final ServerPlayerEntity player;

    private final List<ServerGlyph> glyphs = new ArrayList<>();
    private ServerGlyph drawing;

    private boolean preparedSpell;

    public ServerSpellcastingSource(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public boolean canDraw() {
        return !this.preparedSpell;
    }

    void startDrawing(ServerGlyph glyph) {
        if (this.canDraw()) {
            this.drawing = glyph;
        }
    }

    void addGlyph(ServerGlyph glyph) {
        this.glyphs.add(glyph);
        this.notifyGlyphCreate(glyph);
    }

    void updateDrawingShape(int shape) {
        if (!this.canDraw()) {
            return;
        }

        ServerGlyph glyph = this.drawing;
        if (glyph != null) {
            glyph.setShape(shape);

            GlyphShapeStorage glyphShapes = GlyphShapeStorage.get(this.player.server);

            GlyphType glyphType = glyph.tryMatchGlyph(glyphShapes);
            if (glyphType != null) {
                PacketByteBuf packet = FinishGlyphS2CPacket.create(glyph, glyphType);
                FinishGlyphS2CPacket.sendTo(this.player, packet);
            }

            this.notifyGlyphUpdate(glyph);
        }
    }

    void updateDrawingStroke(@Nullable GlyphNode node) {
        if (!this.canDraw()) {
            return;
        }

        ServerGlyph glyph = this.drawing;
        if (glyph != null) {
            glyph.setStroke(node);
            this.notifyGlyphUpdate(glyph);
        }
    }

    boolean removeGlyph(ServerGlyph glyph) {
        if (this.glyphs.remove(glyph)) {
            if (this.drawing == glyph) {
                this.drawing = null;
            }
            this.notifyGlyphRemove(glyph);
            return true;
        }
        return false;
    }

    ServerGlyph stopDrawing() {
        ServerGlyph drawing = this.drawing;
        this.drawing = null;
        return drawing;
    }

    void prepareSpell() {
        if (!this.preparedSpell) {
            this.preparedSpell = true;
            this.notifyPrepareSpell();
        }
    }

    void cancelSpell() {
        if (this.preparedSpell) {
            this.preparedSpell = false;
            this.clearGlyphs();
        }
    }

    List<ServerGlyph> clearGlyphs() {
        List<ServerGlyph> glyphs = new ArrayList<>(this.glyphs);
        for (ServerGlyph glyph : glyphs) {
            this.notifyGlyphRemove(glyph);
        }
        this.glyphs.clear();
        return glyphs;
    }

    void onStartTracking(ServerPlayerEntity player) {
        for (ServerGlyph glyph : this.glyphs) {
            PacketByteBuf packet = CreateGlyphS2CPacket.create(glyph);
            CreateGlyphS2CPacket.sendTo(player, packet);
        }

        if (this.preparedSpell) {
            PacketByteBuf packet = SetPreparedSpellS2CPacket.create(this.player, false);
            SetPreparedSpellS2CPacket.sendTo(player, packet);
        }
    }

    void onStopTracking(ServerPlayerEntity player) {
        for (ServerGlyph glyph : this.glyphs) {
            PacketByteBuf packet = RemoveGlyphS2CPacket.create(glyph.networkId());
            RemoveGlyphS2CPacket.sendTo(player, packet);
        }
    }

    void notifyGlyphCreate(ServerGlyph glyph) {
        PacketByteBuf packet = CreateGlyphS2CPacket.create(glyph);
        for (ServerPlayerEntity trackingPlayer : this.getTrackingPlayers()) {
            CreateGlyphS2CPacket.sendTo(trackingPlayer, packet);
        }
    }

    void notifyGlyphUpdate(ServerGlyph glyph) {
        PacketByteBuf packet = UpdateGlyphS2CPacket.create(glyph);
        for (ServerPlayerEntity trackingPlayer : this.getTrackingPlayers()) {
            UpdateGlyphS2CPacket.sendTo(trackingPlayer, packet);
        }
    }

    void notifyGlyphRemove(ServerGlyph glyph) {
        PacketByteBuf packet = RemoveGlyphS2CPacket.create(glyph.networkId());
        for (ServerPlayerEntity trackingPlayer : this.getTrackingPlayers()) {
            RemoveGlyphS2CPacket.sendTo(trackingPlayer, packet);
        }
    }

    void notifyPrepareSpell() {
        PacketByteBuf packet = SetPreparedSpellS2CPacket.create(this.player, true);
        SetPreparedSpellS2CPacket.sendTo(this.player, packet);
        for (ServerPlayerEntity trackingPlayer : this.getTrackingPlayers()) {
            SetPreparedSpellS2CPacket.sendTo(trackingPlayer, packet);
        }
    }

    Collection<ServerPlayerEntity> getTrackingPlayers() {
        return PlayerLookup.tracking(this.player);
    }
}
