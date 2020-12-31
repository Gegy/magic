package dev.gegy.magic.client.glyph;

import dev.gegy.magic.glyph.Glyph;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.util.Set;

public final class ClientGlyphTracker {
    public static final ClientGlyphTracker INSTANCE = new ClientGlyphTracker();

    private final Set<Glyph> glyphs = new ReferenceOpenHashSet<>();

    public void add(Glyph glyph) {
        this.glyphs.add(glyph);
    }

    public void remove(Glyph glyph) {
        this.glyphs.remove(glyph);
    }

    public Set<Glyph> getGlyphs() {
        return this.glyphs;
    }
}
