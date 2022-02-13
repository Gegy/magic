package dev.gegy.magic.client.glyph.spell.transform;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.math.Vec3f;

public interface SpellTransform extends GlyphTransform {
    default void tick() {
    }

    @Override
    Vec3f getOrigin(float tickDelta);

    @Override
    Vec3f getDirection(float tickDelta);

    GlyphTransform getTransformForGlyph(int index);
}
