package dev.gegy.magic.client.glyph.render;

import dev.gegy.magic.glyph.GlyphStroke;
import net.minecraft.util.math.Matrix4f;

public final class GlyphRenderData {
    public Matrix4f glyphToWorld = new Matrix4f();
    public float centerX;
    public float centerY;
    public float radius;
    public float formProgress;
    public float red;
    public float green;
    public float blue;
    public int edges;

    public GlyphStroke stroke;
}
