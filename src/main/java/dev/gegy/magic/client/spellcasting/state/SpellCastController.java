package dev.gegy.magic.client.spellcasting.state;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.glyph.GlyphType;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class SpellCastController {
    private SpellCastState state;

    public void tick(ClientPlayerEntity player) {
        var state = this.state;
        if (state == null) {
            this.state = state = new BeginDraw();
        }
        this.state = state.tick(player);
    }

    public void clear() {
        this.state = null;
    }

    @Nullable
    public ClientGlyph finishDrawingGlyph(GlyphType matchedType) {
        var state = this.state;
        if (state != null) {
            var result = state.finishDrawingGlyph(matchedType);
            this.state = result.state();
            return result.glyph();
        }
        return null;
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        var state = this.state;
        if (state != null) {
            return state.getDrawingGlyph();
        }
        return null;
    }
}
