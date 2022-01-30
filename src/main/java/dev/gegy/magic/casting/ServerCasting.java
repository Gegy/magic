package dev.gegy.magic.casting;

import dev.gegy.magic.client.casting.ConfiguredClientCasting;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface ServerCasting {
    @Nullable
    ServerCasting.Factory tick();

    void handleEvent(Identifier id, PacketByteBuf buf);

    @Nullable
    ConfiguredClientCasting<?> createClientCasting();

    interface Factory {
        ServerCasting build(ServerPlayerEntity player, ServerCastingBuilder casting);
    }
}
