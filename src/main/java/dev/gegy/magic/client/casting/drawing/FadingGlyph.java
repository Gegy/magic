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

    public FadingGlyph(SpellSource source, GlyphPlane plane, GlyphForm form, AnimationTimer timer) {
        this.source = source;
        this.plane = plane;
        this.form = form;
        this.timer = timer;
    }

    public float getOpacity(float tickDelta) {
        return 1.0F - Easings.easeOutCirc(this.timer.getProgress(tickDelta));
    }

    public SpellSource source() {
        return this.source;
    }

    public GlyphPlane plane() {
        return this.plane;
    }

    public GlyphForm form() {
        return this.form;
    }
}
