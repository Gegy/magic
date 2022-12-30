package dev.gegy.magic.client.casting.drawing;

import dev.gegy.magic.client.glyph.GlyphPlane;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.math.AnimationTimer;
import dev.gegy.magic.math.Easings;

public final class FadingGlyph {
    private final SpellSource source;
    private final GlyphPlane plane;
    private final GlyphForm form;

    private final AnimationTimer timer;

    public FadingGlyph(final SpellSource source, final GlyphPlane plane, final GlyphForm form, final AnimationTimer timer) {
        this.source = source;
        this.plane = plane;
        this.form = form;
        this.timer = timer;
    }

    public float getOpacity(final float tickDelta) {
        return 1.0f - Easings.easeOutCirc(timer.getProgress(tickDelta));
    }

    public SpellSource source() {
        return source;
    }

    public GlyphPlane plane() {
        return plane;
    }

    public GlyphForm form() {
        return form;
    }
}
