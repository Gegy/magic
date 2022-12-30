package dev.gegy.magic.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SparkParticle extends TextureSheetParticle {
    private final float scale;
    private final Vector3f vector = new Vector3f();

    SparkParticle(final ClientLevel level, final double x, final double y, final double z, final double velocityX, final double velocityY, final double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        xd = velocityX;
        yd = velocityY;
        zd = velocityZ;

        scale = 0.025f + random.nextFloat() * 0.025f;
        lifetime = random.nextInt(20) + 20;

        // TODO
        gCol = 0.4F;
        bCol = 0.3F;
        alpha = 0.9F;

        gravity = 0.0f;
        friction = 1.0f;

        hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(final VertexConsumer writer, final Camera camera, final float tickDelta) {
        final Vec3 cameraPos = camera.getPosition();
        final float dx = (float) (Mth.lerp(tickDelta, xo, x) - cameraPos.x());
        final float dy = (float) (Mth.lerp(tickDelta, yo, y) - cameraPos.y());
        final float dz = (float) (Mth.lerp(tickDelta, zo, z) - cameraPos.z());

        final Quaternionf cameraRotation = camera.rotation();

        final float scale = quadSize;
        final int light = getLightColor(tickDelta);
        final float alpha = getAlpha(tickDelta);

        final float minU = getU0();
        final float minV = getV0();
        final float maxU = getU1();
        final float maxV = getV1();

        vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, -1.0f, -1.0f, minU, minV);
        vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, -1.0f, 1.0f, minU, maxV);
        vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, 1.0f, 1.0f, maxU, maxV);
        vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, 1.0f, -1.0f, maxU, minV);
    }

    private void vertex(
            final VertexConsumer writer,
            final Quaternionf rotation, final float dx, final float dy, final float dz,
            final float scale, final int light, final float alpha,
            final float x, final float y, final float u, final float v
    ) {
        final Vector3f vertex = vector.set(x, y, 0.0f)
                .rotate(rotation)
                .mul(scale)
                .add(dx, dy, dz);

        writer.vertex(vertex.x(), vertex.y(), vertex.z())
                .uv(u, v)
                .color(rCol, gCol, bCol, alpha)
                .uv2(light)
                .endVertex();
    }

    private float getAlpha(final float tickDelta) {
        final float age = this.age + tickDelta;
        final float animation = Mth.clamp((lifetime - age) / 5.0f, 0.0f, 1.0f);
        return animation * alpha;
    }

    @Override
    public int getLightColor(final float tickDelta) {
        final int blockLight = 15 * 16;
        final int skyLight = 15 * 16;
        return blockLight | skyLight << 16;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(final SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(final SimpleParticleType type, final ClientLevel level, final double x, final double y, final double z, final double velocityX, final double velocityY, final double velocityZ) {
            final SparkParticle particle = new SparkParticle(level, x, y, z, velocityX, velocityY, velocityZ);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
