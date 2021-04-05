package dev.gegy.magic.client.render.glyph;

import dev.gegy.magic.client.glyph.GlyphStroke;
import net.minecraft.util.math.Matrix4f;

public final class GlyphRenderData {
    public final Matrix4f glyphToWorld = new Matrix4f();
    public float radius;
    public float formProgress;
    public float primaryRed, primaryGreen, primaryBlue;
    public float secondaryRed, secondaryGreen, secondaryBlue;
    public int shape;

    public boolean highlightNodes;
    public GlyphStroke stroke;
}
