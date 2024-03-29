package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.glyph.GlyphType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class SpellCasting {
    @Nullable
    public static GlyphType.CastFunction cast(final List<GlyphType> glyphs) {
        // TODO: handle spellcasting of combined glyphs
        if (glyphs.size() == 1) {
            return glyphs.get(0).castFunction();
        }

        return null;
    }
}
