package dev.gegy.magic.mixin.client;

import dev.gegy.magic.client.animator.SpellcastingAnimatableEntity;
import dev.gegy.magic.client.animator.SpellcastingAnimator;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements SpellcastingAnimatableEntity {
    @Unique
    private final SpellcastingAnimator spellcastingAnimator = new SpellcastingAnimator();

    @Override
    public SpellcastingAnimator getSpellcastingAnimator() {
        return this.spellcastingAnimator;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        this.spellcastingAnimator.tick((LivingEntity) (Object) this);
    }
}
