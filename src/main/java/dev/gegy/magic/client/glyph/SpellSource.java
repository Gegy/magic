package dev.gegy.magic.client.glyph;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

public interface SpellSource {
    static SpellSource of(Entity sourceEntity) {
        return new SpellSource() {
            @Override
            public Vec3d getPosition(float tickDelta) {
                return sourceEntity.getCameraPosVec(tickDelta);
            }

            @Override
            public Vec3d getLookVector(float tickDelta) {
                return sourceEntity.getRotationVec(tickDelta);
            }

            @Override
            public boolean matchesEntity(Entity entity) {
                return sourceEntity == entity;
            }
        };
    }

    Vec3d getPosition(float tickDelta);

    Vec3d getLookVector(float tickDelta);

    default boolean matchesEntity(Entity entity) {
        return false;
    }
}
