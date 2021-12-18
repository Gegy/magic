package dev.gegy.magic.client.spellcasting;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import dev.gegy.magic.client.event.ClientRemoveEntityEvent;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.transform.GlyphPlane;
import dev.gegy.magic.client.glyph.transform.PreparedGlyphTransform;
import dev.gegy.magic.client.spellcasting.state.SpellCastController;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.glyph.GlyphType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public final class ClientSpellcastingTracker {
    public static final ClientSpellcastingTracker INSTANCE = new ClientSpellcastingTracker();

    private final SpellCastController drawController = new SpellCastController();

    static {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
        ClientRemoveEntityEvent.EVENT.register(INSTANCE::removeEntity);
    }

    private final Int2ObjectMap<ClientGlyph> glyphsById = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<ClientSpellcastingSource> sources = new Int2ObjectOpenHashMap<>();

    private final Collection<ClientGlyph> allGlyphs = new GlyphsCollection();

    private ClientSpellcastingTracker() {
    }

    private void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            this.glyphsById.clear();
            this.sources.clear();
            this.drawController.clear();
            return;
        }

        this.drawController.tick(player);

        this.glyphsById.values().removeIf(ClientGlyph::tick);

        ClientSpellcastingSource source = this.getOrCreateSource(player);
        source.setDrawingGlyph(this.drawController.getDrawingGlyph());
    }

    public ClientGlyph addGlyph(int networkId, Entity entity, GlyphPlane plane, float radius, int shape) {
        long time = entity.world.getTime();

        ClientSpellcastingSource source = this.getOrCreateSource(entity);

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
            ClientSpellcastingSource source = this.getSource(glyph.source);
            if (source != null) {
                source.removeGlyph(glyph);
            }
            return glyph;
        }

        return null;
    }

    @Nullable
    public ClientGlyph getOwnDrawingGlyph() {
        return this.drawController.getDrawingGlyph();
    }

    public Collection<ClientGlyph> getGlyphs() {
        return this.allGlyphs;
    }

    @NotNull
    public List<ClientGlyph> getPreparedGlyphsFor(Entity entity) {
        ClientSpellcastingSource source = this.getSource(entity);
        if (source != null && source.isPrepared()) {
            return source.getGlyphs();
        } else {
            return ImmutableList.of();
        }
    }

    @Nullable
    public ClientGlyph getDrawingGlyphFor(Entity entity) {
        ClientSpellcastingSource source = this.getSource(entity);
        return source != null ? source.getDrawingGlyph() : null;
    }

    @Nullable
    private ClientSpellcastingSource getSource(Entity entity) {
        return this.sources.get(entity.getId());
    }

    @NotNull
    private ClientSpellcastingSource getOrCreateSource(Entity entity) {
        return this.sources.computeIfAbsent(entity.getId(), i -> new ClientSpellcastingSource());
    }

    public void updateGlyph(int networkId, int shape, @Nullable GlyphNode stroke, @Nullable GlyphType matchedType) {
        ClientGlyph glyph = this.getGlyphById(networkId);
        if (glyph != null) {
            glyph.shape = shape;
            if (matchedType != null) {
                glyph.applyMatchedType(matchedType);
            } else {
                glyph.applyStroke(stroke);
            }

            ClientSpellcastingSource source = this.getOrCreateSource(glyph.source);
            source.setDrawingGlyph(glyph);
        }
    }

    public void finishDrawingOwnGlyph(int networkId, GlyphType matchedType) {
        ClientGlyph glyph = this.drawController.finishDrawingGlyph(matchedType);
        if (glyph != null) {
            glyph.setNetworkId(networkId);
            this.glyphsById.put(networkId, glyph);

            ClientSpellcastingSource source = this.getOrCreateSource(glyph.source);
            source.addGlyph(glyph);
        }
    }

    public void prepareSpellFor(Entity entity, boolean animate) {
        ClientSpellcastingSource source = this.getSource(entity);
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
        ClientSpellcastingSource source = this.getSource(entity);
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

    private void removeEntity(Entity entity) {
        this.sources.remove(entity.getId());
    }

    final class GlyphsCollection extends AbstractCollection<ClientGlyph> {
        @Override
        public Iterator<ClientGlyph> iterator() {
            ClientGlyph drawingGlyph = ClientSpellcastingTracker.this.getOwnDrawingGlyph();
            if (drawingGlyph != null) {
                return Iterators.concat(
                        ClientSpellcastingTracker.this.glyphsById.values().iterator(),
                        Iterators.singletonIterator(drawingGlyph)
                );
            } else {
                return ClientSpellcastingTracker.this.glyphsById.values().iterator();
            }
        }

        @Override
        public int size() {
            ClientGlyph drawingGlyph = ClientSpellcastingTracker.this.getOwnDrawingGlyph();
            if (drawingGlyph != null) {
                return ClientSpellcastingTracker.this.glyphsById.size() + 1;
            } else {
                return ClientSpellcastingTracker.this.glyphsById.size();
            }
        }
    }
}
