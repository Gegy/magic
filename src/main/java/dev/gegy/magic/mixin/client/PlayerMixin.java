package dev.gegy.magic.mixin.client;

import dev.gegy.magic.client.animator.CastingAnimatableEntity;
import dev.gegy.magic.client.animator.CastingAnimator;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements CastingAnimatableEntity {
    @Unique
    private final CastingAnimator castingAnimator = new CastingAnimator();

    private PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @Override
    public CastingAnimator getCastingAnimator() {
        return this.castingAnimator;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (this.level.isClientSide) {
            this.castingAnimator.tick((Player) (Object) this);
        }
    }
}
