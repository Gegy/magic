package dev.gegy.magic.client.glyph;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ClientGlyphSource {
    private final Entity entity;

    private ClientGlyph drawingGlyph;
    private List<ClientGlyph> preparedGlyphs;

    public ClientGlyphSource(Entity entity) {
        this.entity = entity;
    }

    void setDrawingGlyph(ClientGlyph glyph) {
        this.drawingGlyph = glyph;
        this.preparedGlyphs = null;
    }

    void setPreparedGlyphs(List<ClientGlyph> glyphs) {
        this.drawingGlyph = null;
        this.preparedGlyphs = glyphs;
    }

    void removeGlyph(ClientGlyph glyph) {
        if (this.drawingGlyph == glyph) {
            this.drawingGlyph = null;
        }

        List<ClientGlyph> preparedGlyphs = this.preparedGlyphs;
        if (preparedGlyphs != null) {
            preparedGlyphs.remove(glyph);
        }
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        return this.drawingGlyph;
    }

    @Nullable
    public List<ClientGlyph> getPreparedGlyphs() {
        return this.preparedGlyphs;
    }

    public boolean isEmpty() {
        return this.drawingGlyph == null && this.preparedGlyphs == null;
    }
}
