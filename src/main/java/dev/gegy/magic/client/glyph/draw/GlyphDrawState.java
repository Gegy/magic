package dev.gegy.magic.client.glyph.draw;

import net.minecraft.client.network.ClientPlayerEntity;

interface GlyphDrawState {
    GlyphDrawState tick(ClientPlayerEntity player);
}
