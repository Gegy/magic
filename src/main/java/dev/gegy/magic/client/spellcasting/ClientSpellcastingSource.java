package dev.gegy.magic.client.spellcasting;

import dev.gegy.magic.client.glyph.ClientGlyph;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ClientSpellcastingSource {
    private final List<ClientGlyph> glyphs = new ArrayList<>();
    private ClientGlyph drawingGlyph;

    private boolean prepared;

    void setDrawingGlyph(ClientGlyph glyph) {
        this.drawingGlyph = glyph;
        if (glyph != null) {
            this.prepared = false;
        }
    }

    List<ClientGlyph> prepareGlyphs() {
        if (!this.prepared && !this.glyphs.isEmpty()) {
            this.prepared = true;
            this.glyphs.sort(Comparator.comparingDouble(glyph -> glyph.radius));
            this.drawingGlyph = null;
        }
        return this.glyphs;
    }

    List<ClientGlyph> clearGlyphs() {
        List<ClientGlyph> glyphs = new ArrayList<>(this.glyphs);
        this.glyphs.clear();
        this.drawingGlyph = null;
        return glyphs;
    }

    void addGlyph(ClientGlyph glyph) {
        this.glyphs.add(glyph);
    }

    void removeGlyph(ClientGlyph glyph) {
        if (this.drawingGlyph == glyph) {
            this.drawingGlyph = null;
        }

        this.glyphs.remove(glyph);
    }

    public List<ClientGlyph> getGlyphs() {
        return this.glyphs;
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        return this.drawingGlyph;
    }

    public boolean isPrepared() {
        return this.prepared;
    }
}
