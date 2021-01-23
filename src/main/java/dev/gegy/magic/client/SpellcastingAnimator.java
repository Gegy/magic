package dev.gegy.magic.client;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.client.glyph.plane.GlyphTransform;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;

import java.util.List;

// TODO: interpolate between poses
public final class SpellcastingAnimator {
    private static final Vector3f VECTOR = new Vector3f();
    private static final Vector3f POINT = new Vector3f();

    public static void animate(LivingEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta) {
        ClientGlyphTracker glyphTracker = ClientGlyphTracker.INSTANCE;
        ClientGlyph drawingGlyph = glyphTracker.getDrawingGlyphFor(entity);
        if (drawingGlyph != null) {
            animateDrawing(entity, leftArm, rightArm, tickDelta, drawingGlyph);
            return;
        }

        List<ClientGlyph> preparedGlyphs = glyphTracker.getPreparedGlyphsFor(entity);
        if (!preparedGlyphs.isEmpty()) {
            animatePrepared(entity, leftArm, rightArm, tickDelta, preparedGlyphs);
        }
    }

    private static void animateDrawing(LivingEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, ClientGlyph glyph) {
        Vector3f point = POINT;
        glyph.getLookingAt(point, tickDelta);

        float leftX = Math.abs(point.getX());
        float rightX = -leftX;

        Vector3f target = VECTOR;
        target.set(leftX, point.getY(), point.getZ());
        pointPartTowardsOnPlane(entity, leftArm, glyph.transform, target, tickDelta);

        target.set(rightX, point.getY(), point.getZ());
        pointPartTowardsOnPlane(entity, rightArm, glyph.transform, target, tickDelta);
    }

    private static void animatePrepared(LivingEntity entity, ModelPart leftArm, ModelPart rightArm, float tickDelta, List<ClientGlyph> preparedGlyphs) {
        ModelPart mainArm = entity.getMainArm() == Arm.LEFT ? leftArm : rightArm;
        ModelPart otherArm = entity.getMainArm() == Arm.LEFT ? rightArm : leftArm;

        ClientGlyph glyph = preparedGlyphs.get(0);

        Vector3f direction = glyph.transform.getDirection(tickDelta);
        rotateVectorRelativeToBody(direction, entity, tickDelta);

        float distance = glyph.transform.getDistance(tickDelta);

        Vector3f target = VECTOR;
        target.set(
                direction.getX() * distance,
                entity.getStandingEyeHeight() + direction.getY() * distance,
                direction.getZ() * distance
        );

        pointPartTowards(mainArm, target);

        if (preparedGlyphs.size() > 1) {
            pointPartTowards(otherArm, target);
        }
    }

    private static void pointPartTowardsOnPlane(LivingEntity entity, ModelPart part, GlyphTransform transform, Vector3f target, float tickDelta) {
        transform.projectFromPlane(target, tickDelta);
        rotateVectorRelativeToBody(target, entity, tickDelta);
        target.add(0.0F, entity.getStandingEyeHeight(), 0.0F);

        pointPartTowards(part, target);
    }

    private static void pointPartTowards(ModelPart part, Vector3f target) {
        float deltaX = (target.getX() * 16.0F) - part.pivotX;
        float deltaY = (24.0F - target.getY() * 16.0F) - part.pivotY;
        float deltaZ = (target.getZ() * 16.0F) - part.pivotZ;
        double deltaXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        part.yaw = (float) (Math.atan2(deltaZ, deltaX) + Math.PI / 2.0);
        part.pitch = (float) Math.atan2(deltaXZ, deltaY);
    }

    private static void rotateVectorRelativeToBody(Vector3f vector, LivingEntity entity, float tickDelta) {
        float yaw = getBodyYaw(entity, tickDelta);
        rotateVectorY(vector, (float) -Math.toRadians(yaw));
    }

    private static void rotateVectorY(Vector3f vector, float rotationY) {
        float x = vector.getX();
        float y = vector.getY();
        float z = vector.getZ();
        vector.set(
                x * MathHelper.cos(rotationY) - z * MathHelper.sin(rotationY),
                y,
                x * MathHelper.sin(rotationY) + z * MathHelper.cos(rotationY)
        );
    }

    private static float getBodyYaw(LivingEntity entity, float tickDelta) {
        return MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw);
    }
}
