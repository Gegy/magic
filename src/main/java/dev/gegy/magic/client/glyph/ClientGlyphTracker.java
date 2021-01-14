package dev.gegy.magic.client.glyph;

import com.google.common.collect.ImmutableList;
import dev.gegy.magic.client.glyph.plane.GlyphPlane;
import dev.gegy.magic.client.glyph.plane.PreparedGlyphTransform;
import dev.gegy.magic.client.glyph.spellcasting.SpellcastingController;
import dev.gegy.magic.spell.Spell;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class ClientGlyphTracker {
    public static final ClientGlyphTracker INSTANCE = new ClientGlyphTracker();

    private final SpellcastingController spellcasting = new SpellcastingController();

    static {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
    }

    private final Int2ObjectMap<ClientGlyph> glyphsById = new Int2ObjectOpenHashMap<>();

    private final Int2ObjectMap<ClientGlyph> drawingGlyphsBySource = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<List<ClientGlyph>> preparedGlyphsBySource = new Int2ObjectOpenHashMap<>();

    private ClientGlyphTracker() {
    }

    private void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            this.glyphsById.clear();
            this.spellcasting.clear();
            return;
        }

        this.spellcasting.tick(player);

        this.glyphsById.values().removeIf(ClientGlyph::tick);
    }

    public ClientGlyph addGlyph(int networkId, Entity source, GlyphPlane plane, float radius, int shape) {
        long time = source.world.getTime();

        ClientGlyph glyph = new ClientGlyph(source, plane, radius, time);
        glyph.shape = shape;

        this.glyphsById.put(networkId, glyph);

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
            int sourceId = glyph.source.getEntityId();
            this.drawingGlyphsBySource.remove(sourceId, glyph);

            List<ClientGlyph> prepared = this.preparedGlyphsBySource.get(sourceId);
            if (prepared != null && prepared.remove(glyph) && prepared.isEmpty()) {
                this.preparedGlyphsBySource.remove(sourceId);
            }

            return glyph;
        }

        return null;
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        return this.spellcasting.getDrawingGlyph();
    }

    public Collection<ClientGlyph> getGlyphs() {
        return this.glyphsById.values();
    }

    @NotNull
    public List<ClientGlyph> getPreparedGlyphsFor(Entity source) {
        return this.preparedGlyphsBySource.getOrDefault(source.getEntityId(), ImmutableList.of());
    }

    @Nullable
    public ClientGlyph getDrawingGlyphFor(Entity source) {
        ClientGlyph ownDrawingGlyph = this.spellcasting.getDrawingGlyph();
        if (ownDrawingGlyph != null && ownDrawingGlyph.source == source) {
            return ownDrawingGlyph;
        }

        return this.drawingGlyphsBySource.get(source.getEntityId());
    }

    public void updateGlyph(int networkId, int shape, @Nullable Spell matchedSpell) {
        ClientGlyph glyph = this.getGlyphById(networkId);
        if (glyph != null) {
            glyph.shape = shape;
            if (matchedSpell != null) {
                glyph.applySpell(matchedSpell);
            }

            this.drawingGlyphsBySource.put(glyph.source.getEntityId(), glyph);
        }
    }

    public void finishDrawingGlyph(int networkId, Spell spell) {
        ClientGlyph glyph = this.spellcasting.finishDrawingGlyph(spell);
        if (glyph != null) {
            this.glyphsById.put(networkId, glyph);
        }
    }

    // TODO: maybe worth indexing by source- but the client will never have many glyphs tracked
    public void prepareSpellFor(Entity source) {
        List<ClientGlyph> glyphs = this.collectGlyphsForSource(source);
        glyphs.sort(Comparator.comparingDouble(value -> value.radius));

        float glyphSpacing = 0.1F;
        for (int i = 0; i < glyphs.size(); i++) {
            ClientGlyph glyph = glyphs.get(i);
            float distance = GlyphPlane.DRAW_DISTANCE + glyphSpacing * i;
            glyph.transform = new PreparedGlyphTransform(source, glyph.transform, distance);
        }

        this.preparedGlyphsBySource.put(source.getEntityId(), glyphs);
        this.drawingGlyphsBySource.remove(source.getEntityId());
    }

    private List<ClientGlyph> collectGlyphsForSource(Entity source) {
        List<ClientGlyph> glyphs = new ArrayList<>();
        for (ClientGlyph glyph : this.glyphsById.values()) {
            if (glyph.source == source) {
                glyphs.add(glyph);
            }
        }
        return glyphs;
    }
}
