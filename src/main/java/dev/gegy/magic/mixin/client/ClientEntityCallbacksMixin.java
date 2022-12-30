package dev.gegy.magic.mixin.client;

import dev.gegy.magic.client.event.ClientRemoveEntityEvent;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/multiplayer/ClientLevel$EntityCallbacks")
public class ClientEntityCallbacksMixin {
    @Inject(method = "onDestroyed(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
    private void destroy(final Entity entity, final CallbackInfo ci) {
        ClientRemoveEntityEvent.EVENT.invoker().onRemoveEntity(entity);
    }
}
