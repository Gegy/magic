package dev.gegy.magic.network.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.spellcasting.ServerSpellcastingTracker;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public final class CancelSpellC2SPacket {
    private static final Identifier CHANNEL = Magic.identifier("cancel_spell");

    static void registerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(CHANNEL, (server, player, handler, buf, responseSender) -> {
            server.submit(() -> {
                ServerSpellcastingTracker.INSTANCE.cancelSpell(player);
            });
        });
    }

    public static void sendToServer() {
        ClientPlayNetworking.send(CHANNEL, PacketByteBufs.empty());
    }
}
