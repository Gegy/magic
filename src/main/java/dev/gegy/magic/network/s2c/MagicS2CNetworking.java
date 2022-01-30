package dev.gegy.magic.network.s2c;

public final class MagicS2CNetworking {
    public static void registerReceivers() {
        SetCastingS2CPacket.registerReceiver();
        CastingEventS2CPacket.registerReceiver();
    }
}
