package dev.gegy.magic.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;

public final class GlyphDrawTrailParticle extends SpriteBillboardParticle {
    GlyphDrawTrailParticle(ClientWorld world, double x, double y, double z) {
        super(world, x, y, z);
        this.scale *= 0.25F;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider sprites;

        public Factory(SpriteProvider sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(DefaultParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            GlyphDrawTrailParticle particle = new GlyphDrawTrailParticle(world, x, y, z);
            particle.setSprite(this.sprites);
            return particle;
        }
    }
}
