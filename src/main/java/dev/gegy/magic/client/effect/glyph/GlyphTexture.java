package dev.gegy.magic.client.effect.glyph;

final class GlyphTexture {
    public static final int RESOLUTION = 32;

    public static final int SIZE = RESOLUTION * 2;
    public static final float TEXEL_SIZE = 1.0F / SIZE;

    public static final float RENDER_SIZE = RESOLUTION;
    public static final float RENDER_SCALE = SIZE / RENDER_SIZE;

    private GlyphTexture() {
    }
}
