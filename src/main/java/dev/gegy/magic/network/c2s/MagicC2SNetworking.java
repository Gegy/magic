package dev.gegy.magic.network.c2s;

public final class MagicC2SNetworking {
    public static void registerReceivers() {
        BeginGlyphC2SPacket.registerReceiver();
        DrawGlyphC2SPacket.registerReceiver();
    }
}
