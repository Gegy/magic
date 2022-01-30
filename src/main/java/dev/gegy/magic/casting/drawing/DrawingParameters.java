package dev.gegy.magic.casting.drawing;

import dev.gegy.magic.network.codec.PacketCodec;

import java.util.List;

public final record DrawingParameters(List<DrawingGlyphParameters> glyphs) {
    public static final PacketCodec<DrawingParameters> CODEC = DrawingGlyphParameters.CODEC.list().map(DrawingParameters::new, DrawingParameters::glyphs);
}
