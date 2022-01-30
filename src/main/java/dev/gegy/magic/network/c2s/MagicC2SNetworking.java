package dev.gegy.magic.network.c2s;

public final class MagicC2SNetworking {
    public static void registerReceivers() {
        CastingEventC2SPacket.registerReceiver();
    }
}
