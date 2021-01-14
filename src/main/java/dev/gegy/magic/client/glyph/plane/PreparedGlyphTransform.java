package dev.gegy.magic.client.glyph.plane;

import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

public final class PreparedGlyphTransform implements GlyphTransform {
    private static final float FORM_TICKS = 6.0F;

    private final Entity source;
    private final long startTime;

    private final float initialDistance;
    private final Vector3f initialDirection;

    private final Vector3f prevTargetDirection;
    private final Vector3f targetDirection;

    private final float targetDistance;

    private final Vector3f left = new Vector3f();
    private final Vector3f up = new Vector3f();
    private final Matrix4f matrix = new Matrix4f();

    public PreparedGlyphTransform(Entity source, GlyphTransform initial, float targetDistance) {
        this.source = source;
        this.startTime = source.world.getTime();

        this.initialDistance = initial.getDistance(1.0F);
        this.initialDirection = initial.getDirection(1.0F);

        this.targetDirection = new Vector3f(source.getRotationVec(1.0F));
        this.prevTargetDirection = this.targetDirection.copy();

        this.targetDistance = targetDistance;
    }

    private float getFormProgress(float tickDelta) {
        long time = this.source.world.getTime();
        return Math.min((float) (time - this.startTime) + tickDelta, FORM_TICKS) / FORM_TICKS;
    }

    @Override
    public void tick() {
        Vector3f direction = this.targetDirection;
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
    public Vector3f getDirection(float tickDelta) {
        float formProgress = this.getFormProgress(tickDelta);

        Vector3f initialDirection = this.initialDirection;

        Vector3f prevTargetDirection = this.prevTargetDirection;
        Vector3f targetDirection = this.targetDirection;
        float targetDirectionX = MathHelper.lerp(tickDelta, prevTargetDirection.getX(), targetDirection.getX());
        float targetDirectionY = MathHelper.lerp(tickDelta, prevTargetDirection.getY(), targetDirection.getY());
        float targetDirectionZ = MathHelper.lerp(tickDelta, prevTargetDirection.getZ(), targetDirection.getZ());

        if (formProgress == 1.0F) {
            return new Vector3f(targetDirectionX, targetDirectionY, targetDirectionZ);
        } else {
            return new Vector3f(
                    MathHelper.lerp(formProgress, initialDirection.getX(), targetDirectionX),
                    MathHelper.lerp(formProgress, initialDirection.getY(), targetDirectionY),
                    MathHelper.lerp(formProgress, initialDirection.getZ(), targetDirectionZ)
            );
        }
    }

    @Override
    public Matrix4f getTransformationMatrix(float tickDelta) {
        float distance = this.getDistance(tickDelta);
        Vector3f direction = this.getDirection(tickDelta);

        Matrix4f matrix = this.matrix;

        Vector3f left = this.left;
        left.set(0.0F, 1.0F, 0.0F);
        left.cross(direction);
        left.normalize();

        Vector3f up = this.up;
        up.set(direction.getX(), direction.getY(), direction.getZ());
        up.cross(left);
        up.normalize();

        Matrix4fAccess.set(matrix,
                left.getX(), up.getX(), direction.getX(), 0.0F,
                left.getY(), up.getY(), direction.getY(), 0.0F,
                left.getZ(), up.getZ(), direction.getZ(), 0.0F,
                0.0F, 0.0F, 0.0F, 1.0F
        );

        Matrix4fAccess.scale(matrix, 1.0F, 1.0F, distance);

        return matrix;
    }
}
