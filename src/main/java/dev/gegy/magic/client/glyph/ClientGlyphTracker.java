package dev.gegy.magic.client.glyph;

import com.google.common.collect.ImmutableList;
import dev.gegy.magic.client.glyph.transform.GlyphPlane;
import dev.gegy.magic.client.glyph.transform.PreparedGlyphTransform;
import dev.gegy.magic.client.spellcasting.SpellcastingController;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.spell.Spell;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public final class ClientGlyphTracker {
    public static final ClientGlyphTracker INSTANCE = new ClientGlyphTracker();

    private final SpellcastingController spellcasting = new SpellcastingController();

    static {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
    }

    private final Int2ObjectMap<ClientGlyph> glyphsById = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<ClientGlyphSource> glyphSources = new Int2ObjectOpenHashMap<>();

    private ClientGlyphTracker() {
    }

    private void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            this.glyphsById.clear();
            this.glyphSources.clear();
            this.spellcasting.clear();
            return;
        }

        this.spellcasting.tick(player);

        this.glyphsById.values().removeIf(ClientGlyph::tick);

        ClientGlyph drawingGlyph = this.spellcasting.getDrawingGlyph();
        if (drawingGlyph != null) {
            ClientGlyphSource source = this.getOrCreateSource(player);
            source.setDrawingGlyph(drawingGlyph);
        } else {
            ClientGlyphSource source = this.getSource(player);
            if (source != null) {
                source.setDrawingGlyph(null);
            }
        }
    }

    public ClientGlyph addGlyph(int networkId, Entity entity, GlyphPlane plane, float radius, int shape) {
        long time = entity.world.getTime();

        ClientGlyphSource source = this.getOrCreateSource(entity);

        ClientGlyph glyph = new ClientGlyph(networkId, entity, plane, radius, time);
        glyph.shape = shape;

        this.glyphsById.put(networkId, glyph);
        source.addGlyph(glyph);

        return glyph;
    }

    @Nullable
    public ClientGlyph getGlyphById(int networkId) {
        return this.glyphsById.get(networkId);
    }

    @Nullable
    public ClientGlyph removeGlyph(int networkId) {
        ClientGlyph glyph = this.glyphsById.remove(networkId);
        if (glyph != null) {
            this.removeGlyphFromSource(glyph);
            return glyph;
        }

        return null;
    }

    private void removeGlyphFromSource(ClientGlyph glyph) {
        ClientGlyphSource source = this.getSource(glyph.source);
        if (source != null) {
            source.removeGlyph(glyph);
            if (source.isEmpty()) {
                this.glyphSources.remove(glyph.source.getId());
            }
        }
    }

    @Nullable
    public ClientGlyph getOwnDrawingGlyph() {
        return this.spellcasting.getDrawingGlyph();
    }

    public Collection<ClientGlyph> getGlyphs() {
        return this.glyphsById.values();
    }

    @NotNull
    public List<ClientGlyph> getPreparedGlyphsFor(Entity entity) {
        ClientGlyphSource source = this.getSource(entity);
        if (source != null && source.isPrepared()) {
            return source.getGlyphs();
        } else {
            return ImmutableList.of();
        }
    }

    @Nullable
    public ClientGlyph getDrawingGlyphFor(Entity entity) {
        ClientGlyphSource source = this.getSource(entity);
        return source != null ? source.getDrawingGlyph() : null;
    }

    @Nullable
    private ClientGlyphSource getSource(Entity entity) {
        return this.glyphSources.get(entity.getId());
    }

    @NotNull
    private ClientGlyphSource getOrCreateSource(Entity entity) {
        return this.glyphSources.computeIfAbsent(entity.getId(), i -> new ClientGlyphSource());
    }

    public void updateGlyph(int networkId, int shape, @Nullable GlyphNode stroke, @Nullable Spell matchedSpell) {
        ClientGlyph glyph = this.getGlyphById(networkId);
        if (glyph != null) {
            glyph.shape = shape;
            if (matchedSpell != null) {
                glyph.applySpell(matchedSpell);
            } else {
                glyph.applyStroke(stroke);
            }

            ClientGlyphSource source = this.getOrCreateSource(glyph.source);
            source.setDrawingGlyph(glyph);
        }
    }

    public void finishDrawingOwnGlyph(int networkId, Spell spell) {
        ClientGlyph glyph = this.spellcasting.finishDrawingGlyph(spell);
        if (glyph != null) {
            glyph.setNetworkId(networkId);
            this.glyphsById.put(networkId, glyph);

            ClientGlyphSource source = this.getOrCreateSource(glyph.source);
            source.addGlyph(glyph);
        }
    }

    public void prepareSpellFor(Entity entity, boolean animate) {
        ClientGlyphSource source = this.getSource(entity);
        if (source == null) {
            return;
        }

        List<ClientGlyph> glyphs = source.prepareGlyphs();

        float glyphSpacing = 0.2F;
        for (int i = 0; i < glyphs.size(); i++) {
            ClientGlyph glyph = glyphs.get(i);
            float distance = GlyphPlane.DRAW_DISTANCE + glyphSpacing * i;
            glyph.transform = animate ? new PreparedGlyphTransform(entity, glyph.transform, distance) : new PreparedGlyphTransform(entity, distance);
        }
    }

    public void clearGlyphsFor(Entity entity) {
        ClientGlyphSource source = this.getSource(entity);
        if (source == null) {
            return;
        }

        List<ClientGlyph> glyphs = source.clearGlyphs();
        for (ClientGlyph glyph : glyphs) {
            if (glyph.hasNetworkId()) {
                this.glyphsById.remove(glyph.getNetworkId(), glyph);
            }
        }
    }
}
