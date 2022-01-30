package dev.gegy.magic.mixin.client;

import dev.gegy.magic.client.animator.CastingAnimatableEntity;
import dev.gegy.magic.client.animator.CastingAnimator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements CastingAnimatableEntity {
    @Unique
    private final CastingAnimator castingAnimator = new CastingAnimator();

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Override
    public CastingAnimator getCastingAnimator() {
        return this.castingAnimator;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (this.world.isClient) {
            this.castingAnimator.tick((PlayerEntity) (Object) this);
        }
    }
}
