package dev.gegy.magic.casting;

import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public interface ServerCasting {
    @Nullable
    ServerCasting.Factory tick();

    void handleEvent(ResourceLocation id, FriendlyByteBuf buf);

    @Nullable
    ConfiguredClientCasting<?> createClientCasting();

    interface Factory {
        ServerCasting build(ServerPlayer player, ServerCastingBuilder casting);
    }
}
