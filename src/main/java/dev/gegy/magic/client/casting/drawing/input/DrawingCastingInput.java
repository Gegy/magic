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
    public ClientDrawingGlyph tick(final ClientCastingDrawing casting, final Player player) {
        DrawingInputState state = getStateOrInit();
        this.state = state = state.tick(casting, player);

        return state != null ? state.getDrawingGlyph() : null;
    }

    @NotNull
    private DrawingInputState getStateOrInit() {
        DrawingInputState state = this.state;
        if (state == null) {
            this.state = state = new BeginDraw();
        }
        return state;
    }

    public void finishDrawing(final GlyphType matchedType) {
        final DrawingInputState state = this.state;
        if (state != null) {
            this.state = state.finishDrawingGlyph(matchedType);
        }
    }

    public void cancelDrawing() {
        final DrawingInputState state = this.state;
        if (state != null) {
            this.state = state.cancelDrawingGlyph();
        }
    }
}
