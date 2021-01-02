package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.glyph.draw.GlyphDrawTracker;
import dev.gegy.magic.glyph.GlyphPlane;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public final class ClientGlyphTracker {
    public static final ClientGlyphTracker INSTANCE = new ClientGlyphTracker();

    private final GlyphDrawTracker drawTracker = new GlyphDrawTracker();

    static {
        ClientTickEvents.END_CLIENT_TICK.register(INSTANCE::tick);
    }

    private final Int2ObjectMap<ClientGlyph> glyphsById = new Int2ObjectOpenHashMap<>();

    private ClientGlyphTracker() {
    }

    private void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            this.glyphsById.clear();
            this.drawTracker.clear();
            return;
        }

        this.drawTracker.tick(player);
        this.glyphsById.values().removeIf(ClientGlyph::tick);
    }

    public ClientGlyph addGlyph(int networkId, Entity source, GlyphPlane plane, float radius, int shape) {
        long time = source.world.getTime();

        ClientGlyph glyph = new ClientGlyph(source, plane, radius, 1.0F, 0.0F, 0.0F, time);
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
        return this.glyphsById.remove(networkId);
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        return this.drawTracker.getDrawingGlyph();
    }

    public Collection<ClientGlyph> getGlyphs() {
        return this.glyphsById.values();
    }

    public void finishDrawingGlyph(int networkId) {
        ClientGlyph glyph = this.drawTracker.getDrawingGlyph();
        this.drawTracker.clear();

        if (glyph != null) {
            this.glyphsById.put(networkId, glyph);
        }
    }
}
