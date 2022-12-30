package dev.gegy.magic.client.glyph;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface SpellSource {
    static SpellSource of(Entity sourceEntity) {
        return new SpellSource() {
            @Override
            public Vec3 getPosition(float tickDelta) {
                return sourceEntity.getEyePosition(tickDelta);
            }

            @Override
            public Vec3 getLookVector(float tickDelta) {
                return sourceEntity.getViewVector(tickDelta);
            }

            @Override
            public boolean matchesEntity(Entity entity) {
                return sourceEntity == entity;
            }
        };
    }

    Vec3 getPosition(float tickDelta);

    Vec3 getLookVector(float tickDelta);

    default boolean matchesEntity(Entity entity) {
        return false;
    }
}
