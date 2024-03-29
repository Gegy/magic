package dev.gegy.magic.glyph;

import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.s2c.*;
import dev.gegy.magic.spell.Spell;
import dev.gegy.magic.spell.SpellGlyphStorage;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public final class ServerGlyphSource {
    private final ServerPlayerEntity player;

    private final List<ServerGlyph> glyphs = new ArrayList<>();
    private ServerGlyph drawing;

    private boolean preparedSpell;

    public ServerGlyphSource(ServerPlayerEntity player) {
        this.player = player;
    }

    public ServerPlayerEntity getPlayer() {
        return this.player;
    }

    public UUID getId() {
        return this.player.getUuid();
    }

    public boolean isEmpty() {
        return this.glyphs.isEmpty() && this.drawing == null;
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

            SpellGlyphStorage spellStorage = SpellGlyphStorage.get(this.player.server);

            Spell spell = glyph.tryMatchSpell(spellStorage);
            if (spell != null) {
                PacketByteBuf packet = FinishGlyphS2CPacket.create(glyph, spell);
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

    void clearGlyphs() {
        for (ServerGlyph glyph : this.glyphs) {
            this.notifyGlyphRemove(glyph);
        }
        this.glyphs.clear();
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
            PacketByteBuf packet = RemoveGlyphS2CPacket.create(glyph.getNetworkId());
            RemoveGlyphS2CPacket.sendTo(player, packet);
        }
    }

    void onRemove() {
        for (ServerGlyph glyph : this.glyphs) {
            this.notifyGlyphRemove(glyph);
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
        PacketByteBuf packet = RemoveGlyphS2CPacket.create(glyph.getNetworkId());
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
