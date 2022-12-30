package dev.gegy.magic.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class SparkParticle extends SpriteBillboardParticle {
    private final float scale;
    private final Vector3f vector = new Vector3f();

    SparkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;

        this.scale = 0.025F + this.random.nextFloat() * 0.025F;
        this.maxAge = this.random.nextInt(20) + 20;

        // TODO
        this.green = 0.4F;
        this.blue = 0.3F;
        this.alpha = 0.9F;

        this.gravityStrength = 0.0F;
        this.velocityMultiplier = 1.0F;

        this.collidesWithWorld = false;
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void buildGeometry(VertexConsumer writer, Camera camera, float tickDelta) {
        Vec3d cameraPos = camera.getPos();
        float dx = (float) (MathHelper.lerp(tickDelta, this.prevPosX, this.x) - cameraPos.getX());
        float dy = (float) (MathHelper.lerp(tickDelta, this.prevPosY, this.y) - cameraPos.getY());
        float dz = (float) (MathHelper.lerp(tickDelta, this.prevPosZ, this.z) - cameraPos.getZ());

        Quaternionf cameraRotation = camera.getRotation();

        float scale = this.scale;
        int light = this.getBrightness(tickDelta);
        float alpha = this.getAlpha(tickDelta);

        float minU = this.getMinU();
        float minV = this.getMinV();
        float maxU = this.getMaxU();
        float maxV = this.getMaxV();

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
                .texture(u, v)
                .color(this.red, this.green, this.blue, alpha)
                .light(light)
                .next();
    }

    private float getAlpha(float tickDelta) {
        float age = this.age + tickDelta;
        float animation = MathHelper.clamp((this.maxAge - age) / 5.0F, 0.0F, 1.0F);
        return animation * this.alpha;
    }

    @Override
    public int getBrightness(float tickDelta) {
        int blockLight = 15 * 16;
        int skyLight = 15 * 16;
        return blockLight | skyLight << 16;
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
            SparkParticle particle = new SparkParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSprite(this.sprites);
            return particle;
        }
    }
}
