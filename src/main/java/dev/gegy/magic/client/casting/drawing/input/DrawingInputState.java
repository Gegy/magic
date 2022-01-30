package dev.gegy.magic.client.casting.drawing.input;

import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.glyph.GlyphType;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

interface DrawingInputState {
    DrawingInputState tick(ClientCastingDrawing casting, PlayerEntity player);

    default DrawingInputState finishDrawingGlyph(GlyphType matchedType) {
        return this;
    }

    default DrawingInputState cancelDrawingGlyph() {
        return this;
    }

    @Nullable
    default ClientDrawingGlyph getDrawingGlyph() {
        return null;
    }
}
