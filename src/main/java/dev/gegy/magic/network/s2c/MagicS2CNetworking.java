package dev.gegy.magic.network.s2c;

public final class MagicS2CNetworking {
    public static void registerReceivers() {
        CreateGlyphS2CPacket.registerReceiver();
        RemoveGlyphS2CPacket.registerReceiver();
        UpdateGlyphS2CPacket.registerReceiver();
        FinishGlyphS2CPacket.registerReceiver();
        SetPreparedSpellS2CPacket.registerReceiver();
    }
}
