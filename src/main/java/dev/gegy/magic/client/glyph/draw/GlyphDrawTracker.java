package dev.gegy.magic.client.glyph.draw;

import dev.gegy.magic.client.glyph.ClientGlyph;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class GlyphDrawTracker {
    private GlyphDrawState state;

    public void tick(ClientPlayerEntity player) {
        GlyphDrawState state = this.state;
        if (state == null) {
            this.state = state = new DrawGlyphOutline();
        }
        this.state = state.tick(player);
    }

    public void clear() {
        this.state = null;
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        GlyphDrawState state = this.state;
        if (state != null) {
            return state.getDrawingGlyph();
        }
        return null;
    }
}
