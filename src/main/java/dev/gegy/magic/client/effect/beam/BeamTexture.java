package dev.gegy.magic.client.effect.beam;

final class BeamTexture {
    public static final int RESOLUTION = 8;

    public static final int WIDTH = RESOLUTION * 16;
    public static final int HEIGHT = RESOLUTION * 4;
    public static final float SCALE_X = (float) WIDTH / RESOLUTION;
    public static final float SCALE_Y = (float) HEIGHT / RESOLUTION;

    public static final int END_RESOLUTION = 16;
    public static final int END_SIZE = END_RESOLUTION * 4;
    public static final float END_SCALE = (float) END_SIZE / END_RESOLUTION;

    private BeamTexture() {
    }
}
