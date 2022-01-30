package dev.gegy.magic.mixin.client;

import dev.gegy.magic.client.animator.CastingAnimatableEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
    private PlayerEntityModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
            method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        var client = MinecraftClient.getInstance();
        var cameraEntity = client.cameraEntity != null ? client.cameraEntity : client.player;
        if (cameraEntity == entity && client.options.getPerspective().isFirstPerson()) {
            return;
        }

        if (entity instanceof CastingAnimatableEntity animatable) {
            var animator = animatable.getCastingAnimator();
            animator.applyToModel((PlayerEntity) entity, this.leftArm, this.rightArm, client.getTickDelta());
        }
    }
}
