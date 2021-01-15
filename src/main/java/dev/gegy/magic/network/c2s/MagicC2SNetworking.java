package dev.gegy.magic.network.c2s;

public final class MagicC2SNetworking {
    public static void registerReceivers() {
        BeginGlyphC2SPacket.registerReceiver();
        DrawGlyphShapeC2SPacket.registerReceiver();
        DrawGlyphStrokeC2SPacket.registerReceiver();
        CancelGlyphC2SPacket.registerReceiver();
        PrepareSpellC2SPacket.registerReceiver();
    }
}
