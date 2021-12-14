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
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

public final class MagicSparkParticle extends SpriteBillboardParticle {
    private final float scale;
    private final Vec3f vector = new Vec3f();

    MagicSparkParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.velocityZ = velocityZ;

        this.scale = 0.025F + this.random.nextFloat() * 0.025F;
        this.maxAge = this.random.nextInt(20) + 20;

        // TODO
        this.colorGreen = 0.4F;
        this.colorBlue = 0.3F;
        this.colorAlpha = 0.9F;

        this.gravityStrength = 0.0F;
        this.field_28786 = 1.0F;

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

        Quaternion cameraRotation = camera.getRotation();

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
            Quaternion rotation, float dx, float dy, float dz,
            float scale, int light, float alpha,
            float x, float y, float u, float v
    ) {
        Vec3f vertex = this.vector;
        vertex.set(x, y, 0.0F);
        vertex.rotate(rotation);
        vertex.scale(scale);
        vertex.add(dx, dy, dz);

        writer.vertex(vertex.getX(), vertex.getY(), vertex.getZ())
                .texture(u, v)
                .color(this.colorRed, this.colorGreen, this.colorBlue, alpha)
                .light(light)
                .next();
    }

    private float getAlpha(float tickDelta) {
        float age = this.age + tickDelta;
        float animation = MathHelper.clamp((this.maxAge - age) / 5.0F, 0.0F, 1.0F);
        return animation * this.colorAlpha;
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
            MagicSparkParticle particle = new MagicSparkParticle(world, x, y, z, velocityX, velocityY, velocityZ);
            particle.setSprite(this.sprites);
            return particle;
        }
    }
}
