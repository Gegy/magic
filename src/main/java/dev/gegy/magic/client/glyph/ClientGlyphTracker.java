package dev.gegy.magic.client.glyph;

import com.google.common.collect.ImmutableList;
import dev.gegy.magic.client.glyph.plane.GlyphPlane;
import dev.gegy.magic.client.glyph.plane.PreparedGlyphTransform;
import dev.gegy.magic.client.glyph.spellcasting.SpellcastingController;
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
    private final Int2ObjectMap<ClientGlyphSource> glyphSources = new Int2ObjectOpenHashMap<>();

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

        ClientGlyphSource ownSource = this.getOrCreateSource(player);
        ownSource.setDrawingGlyph(this.spellcasting.getDrawingGlyph());

        this.glyphsById.values().removeIf(ClientGlyph::tick);

        for (ClientGlyphSource source : this.glyphSources.values()) {
            source.tick();
        }
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
            ClientGlyphSource source = this.glyphSources.get(glyph.source.getEntityId());
            if (source != null) {
                source.removeGlyph(glyph);
                if (source.isEmpty()) {
                    this.glyphSources.remove(glyph.source.getEntityId());
                }
            }

            return glyph;
        }

        return null;
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
        if (source != null) {
            List<ClientGlyph> preparedGlyphs = source.getPreparedGlyphs();
            if (preparedGlyphs != null) {
                return preparedGlyphs;
            }
        }

        return ImmutableList.of();
    }

    @Nullable
    public ClientGlyph getDrawingGlyphFor(Entity entity) {
        ClientGlyph ownDrawingGlyph = this.spellcasting.getDrawingGlyph();
        if (ownDrawingGlyph != null && ownDrawingGlyph.source == entity) {
            return ownDrawingGlyph;
        }

        ClientGlyphSource source = this.getSource(entity);
        return source != null ? source.getDrawingGlyph() : null;
    }

    @Nullable
    public ClientGlyphSource getSource(Entity entity) {
        return this.glyphSources.get(entity.getEntityId());
    }

    @NotNull
    private ClientGlyphSource getOrCreateSource(Entity entity) {
        return this.glyphSources.computeIfAbsent(entity.getEntityId(), i -> new ClientGlyphSource(entity));
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
            this.glyphsById.put(networkId, glyph);
        }
    }

    // TODO: maybe worth indexing by source- but the client will never have many glyphs tracked
    public void prepareSpellFor(Entity entity) {
        List<ClientGlyph> glyphs = this.collectGlyphsForSource(entity);
        glyphs.sort(Comparator.comparingDouble(value -> value.radius));

        float glyphSpacing = 0.1F;
        for (int i = 0; i < glyphs.size(); i++) {
            ClientGlyph glyph = glyphs.get(i);
            float distance = GlyphPlane.DRAW_DISTANCE + glyphSpacing * i;
            glyph.transform = new PreparedGlyphTransform(entity, glyph.transform, distance);
        }

        ClientGlyphSource source = this.getOrCreateSource(entity);
        source.setPreparedGlyphs(glyphs);
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
