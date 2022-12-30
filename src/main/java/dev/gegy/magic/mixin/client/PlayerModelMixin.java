package dev.gegy.magic.mixin.client;

import dev.gegy.magic.client.animator.CastingAnimatableEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
    private PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        var client = Minecraft.getInstance();
        var cameraEntity = client.cameraEntity != null ? client.cameraEntity : client.player;
        if (cameraEntity == entity && client.options.getCameraType().isFirstPerson()) {
            return;
        }

        if (entity instanceof CastingAnimatableEntity animatable) {
            var animator = animatable.getCastingAnimator();
            animator.applyToModel((Player) entity, this.leftArm, this.rightArm, client.getFrameTime());
        }
    }
}
