package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public final class CastingEventC2SPacket {
    static final ResourceLocation CHANNEL = Magic.identifier("casting_event");

    public static <T> void sendToServer(final CastingEventSpec<T> spec, final T event) {
        final FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeResourceLocation(spec.id());
        spec.codec().encode(event, buf);

        ClientPlayNetworking.send(CHANNEL, buf);
    }
}
