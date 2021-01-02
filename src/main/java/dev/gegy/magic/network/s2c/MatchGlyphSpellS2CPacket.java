package dev.gegy.magic.network.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.client.glyph.ClientGlyphTracker;
import dev.gegy.magic.glyph.ServerGlyph;
import dev.gegy.magic.spell.Spell;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class MatchGlyphSpellS2CPacket {
    private static final Identifier CHANNEL = Magic.identifier("match_glyph_spell");

    static void registerReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL, (client, handler, buf, responseSender) -> {
            client.submit(() -> {
                ClientGlyph glyph = ClientGlyphTracker.INSTANCE.getDrawingGlyph();
                if (glyph != null) {
                    glyph.applySpell(Spell.TEST);
                }
            });
        });
    }

    public static PacketByteBuf create(ServerGlyph glyph) {
        PacketByteBuf buf = PacketByteBufs.create();
        return buf;
    }

    public static void sendTo(ServerPlayerEntity player, PacketByteBuf buf) {
        ServerPlayNetworking.send(player, CHANNEL, buf);
    }
}
