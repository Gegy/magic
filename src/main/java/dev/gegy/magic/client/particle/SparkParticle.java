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

    SparkParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.xd = velocityX;
        this.yd = velocityY;
        this.zd = velocityZ;

        this.scale = 0.025F + this.random.nextFloat() * 0.025F;
        this.lifetime = this.random.nextInt(20) + 20;

        // TODO
        this.gCol = 0.4F;
        this.bCol = 0.3F;
        this.alpha = 0.9F;

        this.gravity = 0.0F;
        this.friction = 1.0F;

        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(VertexConsumer writer, Camera camera, float tickDelta) {
        Vec3 cameraPos = camera.getPosition();
        float dx = (float) (Mth.lerp(tickDelta, this.xo, this.x) - cameraPos.x());
        float dy = (float) (Mth.lerp(tickDelta, this.yo, this.y) - cameraPos.y());
        float dz = (float) (Mth.lerp(tickDelta, this.zo, this.z) - cameraPos.z());

        Quaternionf cameraRotation = camera.rotation();

        float scale = this.quadSize;
        int light = this.getLightColor(tickDelta);
        float alpha = this.getAlpha(tickDelta);

        float minU = this.getU0();
        float minV = this.getV0();
        float maxU = this.getU1();
        float maxV = this.getV1();

        this.vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, -1.0F, -1.0F, minU, minV);
        this.vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, -1.0F, 1.0F, minU, maxV);
        this.vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, 1.0F, 1.0F, maxU, maxV);
        this.vertex(writer, cameraRotation, dx, dy, dz, scale, light, alpha, 1.0F, -1.0F, maxU, minV);
    }

    private void vertex(
            VertexConsumer writer,
            Quaternionf rotation, float dx, float dy, float dz,
            float scale, int light, float alpha,
            float x, float y, float u, float v
    ) {
        Vector3f vertex = this.vector.set(x, y, 0.0F)
                .rotate(rotation)
                .mul(scale)
                .add(dx, dy, dz);

        writer.vertex(vertex.x(), vertex.y(), vertex.z())
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, alpha)
                .uv2(light)
                .endVertex();
    }

    private float getAlpha(float tickDelta) {
        float age = this.age + tickDelta;
        float animation = Mth.clamp((this.lifetime - age) / 5.0F, 0.0F, 1.0F);
        return animation * this.alpha;
    }

    @Override
    public int getLightColor(float tickDelta) {
        int blockLight = 15 * 16;
        int skyLight = 15 * 16;
        return blockLight | skyLight << 16;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Factory(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            SparkParticle particle = new SparkParticle(level, x, y, z, velocityX, velocityY, velocityZ);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
