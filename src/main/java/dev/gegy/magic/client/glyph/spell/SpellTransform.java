package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public final class SpellTransform implements GlyphTransform {
    private static final float GLYPH_SPACING = 0.2F;

    private final Vec3f direction;
    private final Vec3f prevDirection;
    private final Vec3f interpolatedDirection = new Vec3f();

    private final float castingDistance;

    public SpellTransform(Vec3d direction, float castingDistance) {
        this.direction = new Vec3f(direction);
        this.castingDistance = castingDistance;
        this.prevDirection = this.direction.copy();
    }

    public void tick(Vec3d target) {
        var direction = this.direction;
        this.prevDirection.set(direction);

        direction.set(
                direction.getX() + (float) (target.x - direction.getX()) * 0.5F,
                direction.getY() + (float) (target.y - direction.getY()) * 0.5F,
                direction.getZ() + (float) (target.z - direction.getZ()) * 0.5F
        );
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        var interpolatedDirection = this.interpolatedDirection;
        interpolatedDirection.set(this.prevDirection);
        interpolatedDirection.lerp(this.direction, tickDelta);
        return interpolatedDirection;
    }

    @Override
    public float getDistance(float tickDelta) {
        return this.castingDistance;
    }

    public static float getDistanceForGlyph(int index) {
        return GlyphTransform.DRAW_DISTANCE + index * GLYPH_SPACING;
    }

    public GlyphTransform getTransformForGlyph(int index) {
        float distance = getDistanceForGlyph(index);

        return new GlyphTransform() {
            @Override
            public Vec3f getDirection(float tickDelta) {
                return SpellTransform.this.getDirection(tickDelta);
            }

            @Override
            public float getDistance(float tickDelta) {
                return distance;
            }
        };
    }
}
