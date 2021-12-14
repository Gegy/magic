package dev.gegy.magic.mixin.client;

import dev.gegy.magic.client.event.ClientRemoveEntityEvent;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/world/ClientWorld$ClientEntityHandler")
public class ClientEntityHandlerMixin {
    @Inject(method = "destroy(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void destroy(Entity entity, CallbackInfo ci) {
        ClientRemoveEntityEvent.EVENT.invoker().onRemoveEntity(entity);
    }
}
