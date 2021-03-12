package dev.gegy.magic.mixin;

import dev.gegy.magic.event.LateTrackingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "startTracking", at = @At("TAIL"))
    private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
        LateTrackingEvent.START.invoker().onStartTracking(this.entity, player);
    }
}
