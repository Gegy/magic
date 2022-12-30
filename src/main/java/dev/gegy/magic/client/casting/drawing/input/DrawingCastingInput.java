package dev.gegy.magic.client.casting.drawing.input;

import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.glyph.GlyphType;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class DrawingCastingInput {
    private DrawingInputState state;

    @Nullable
    public ClientDrawingGlyph tick(ClientCastingDrawing casting, Player player) {
        var state = this.getStateOrInit();
        this.state = state = state.tick(casting, player);

        return state != null ? state.getDrawingGlyph() : null;
    }

    @NotNull
    private DrawingInputState getStateOrInit() {
        var state = this.state;
        if (state == null) {
            this.state = state = new BeginDraw();
        }
        return state;
    }

    public void finishDrawing(GlyphType matchedType) {
        var state = this.state;
        if (state != null) {
            this.state = state.finishDrawingGlyph(matchedType);
        }
    }

    public void cancelDrawing() {
        var state = this.state;
        if (state != null) {
            this.state = state.cancelDrawingGlyph();
        }
    }
}
