package dev.gegy.magic.client.glyph.draw;

import dev.gegy.magic.client.glyph.ClientGlyph;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

interface GlyphDrawState {
    GlyphDrawState tick(ClientPlayerEntity player);

    @Nullable
    default ClientGlyph getDrawingGlyph() {
        return null;
    }
}
