package dev.gegy.magic.client.effect.casting.spell.beam.render;

import dev.gegy.magic.casting.spell.beam.ServerCastingBeam;

final class BeamTexture {
    public static final int RESOLUTION = 8;

    public static final int WIDTH = RESOLUTION * ServerCastingBeam.MAXIMUM_LENGTH;
    public static final int HEIGHT = RESOLUTION * 4;
    public static final float SCALE_X = (float) WIDTH / RESOLUTION;
    public static final float SCALE_Y = (float) HEIGHT / RESOLUTION;

    public static final int END_RESOLUTION = 16;
    public static final int END_SIZE = END_RESOLUTION * 4;
    public static final float END_SCALE = (float) END_SIZE / END_RESOLUTION;

    private BeamTexture() {
    }
}
