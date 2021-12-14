package dev.gegy.magic.client.glyph.transform;

import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;

public final class PreparedGlyphTransform implements GlyphTransform {
    private static final float FORM_TICKS = 6.0F;

    private final Entity source;
    private final long startTime;

    private final float initialDistance;
    private final Vec3f initialDirection;

    private final Vec3f prevTargetDirection;
    private final Vec3f targetDirection;

    private final float targetDistance;

    private final Vec3f left = new Vec3f();
    private final Vec3f up = new Vec3f();
    private final Matrix4f matrix = new Matrix4f();

    public PreparedGlyphTransform(Entity source, GlyphTransform initial, float targetDistance) {
        this.source = source;
        this.startTime = source.world.getTime();

        this.initialDistance = initial.getDistance(1.0F);
        this.initialDirection = initial.getDirection(1.0F);

        this.targetDirection = new Vec3f(source.getRotationVec(1.0F));
        this.prevTargetDirection = this.targetDirection.copy();

        this.targetDistance = targetDistance;
    }

    public PreparedGlyphTransform(Entity source, float targetDistance) {
        this.source = source;
        this.startTime = 0;
        this.initialDistance = 1.0F;
        this.initialDirection = Vec3f.ZERO;

        this.targetDirection = new Vec3f(source.getRotationVec(1.0F));
        this.prevTargetDirection = this.targetDirection.copy();

        this.targetDistance = targetDistance;
    }

    private float getFormProgress(float tickDelta) {
        long time = this.source.world.getTime();
        return Math.min((float) (time - this.startTime) + tickDelta, FORM_TICKS) / FORM_TICKS;
    }

    @Override
    public void tick() {
        Vec3f direction = this.targetDirection;
        this.prevTargetDirection.set(direction.getX(), direction.getY(), direction.getZ());

        Vec3d targetDirection = this.source.getRotationVec(1.0F);
        direction.set(
                direction.getX() + (float) (targetDirection.x - direction.getX()) * 0.5F,
                direction.getY() + (float) (targetDirection.y - direction.getY()) * 0.5F,
                direction.getZ() + (float) (targetDirection.z - direction.getZ()) * 0.5F
        );
    }

    @Override
    public float getDistance(float tickDelta) {
        float formProgress = this.getFormProgress(tickDelta);
        return MathHelper.lerp(formProgress, this.initialDistance, this.targetDistance);
    }

    @Override
    public Vec3f getDirection(float tickDelta) {
        float formProgress = this.getFormProgress(tickDelta);

        Vec3f initialDirection = this.initialDirection;

        Vec3f prevTargetDirection = this.prevTargetDirection;
        Vec3f targetDirection = this.targetDirection;
        float targetDirectionX = MathHelper.lerp(tickDelta, prevTargetDirection.getX(), targetDirection.getX());
        float targetDirectionY = MathHelper.lerp(tickDelta, prevTargetDirection.getY(), targetDirection.getY());
        float targetDirectionZ = MathHelper.lerp(tickDelta, prevTargetDirection.getZ(), targetDirection.getZ());

        if (formProgress == 1.0F) {
            return new Vec3f(targetDirectionX, targetDirectionY, targetDirectionZ);
        } else {
            return new Vec3f(
                    MathHelper.lerp(formProgress, initialDirection.getX(), targetDirectionX),
                    MathHelper.lerp(formProgress, initialDirection.getY(), targetDirectionY),
                    MathHelper.lerp(formProgress, initialDirection.getZ(), targetDirectionZ)
            );
        }
    }

    @Override
    public Matrix4f getTransformationMatrix(float tickDelta) {
        Vec3f direction = this.getDirection(tickDelta);

        Matrix4f matrix = this.matrix;

        Vec3f left = this.left;
        left.set(0.0F, 1.0F, 0.0F);
        left.cross(direction);
        left.normalize();

        Vec3f up = this.up;
        up.set(direction.getX(), direction.getY(), direction.getZ());
        up.cross(left);
        up.normalize();

        Matrix4fAccess.set(matrix,
                left.getX(), up.getX(), direction.getX(), 0.0F,
                left.getY(), up.getY(), direction.getY(), 0.0F,
                left.getZ(), up.getZ(), direction.getZ(), 0.0F,
                0.0F, 0.0F, 0.0F, 1.0F
        );

        return matrix;
    }

    @Override
    public void projectOntoPlane(Vec3f vector, float tickDelta) {
        Matrix4f matrix = this.getTransformationMatrix(tickDelta);
        matrix.invert();

        Vector4f transformed = new Vector4f(vector);
        transformed.transform(matrix);

        float distance = this.getDistance(tickDelta);

        // once we're in plane space, move it onto the plane by scaling such that z=distance
        vector.scale(distance / vector.getZ());
    }

    @Override
    public void projectFromPlane(Vec3f vector, float tickDelta) {
        Matrix4f matrix = this.getTransformationMatrix(tickDelta);

        Vector4f transformed = new Vector4f(vector);
        transformed.transform(matrix);

        vector.set(transformed.getX(), transformed.getY(), transformed.getZ());
    }
}
