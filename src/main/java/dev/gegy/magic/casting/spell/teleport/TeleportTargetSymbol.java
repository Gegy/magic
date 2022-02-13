package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.math.ColorRgb;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.PacketByteBuf;

public final record TeleportTargetSymbol(
        char character,
        ColorRgb color
) {
    public static final PacketCodec<TeleportTargetSymbol> CODEC = PacketCodec.of(TeleportTargetSymbol::encode, TeleportTargetSymbol::decode);

    private void encode(PacketByteBuf buf) {
        buf.writeChar(this.character);
        ColorRgb.PACKET_CODEC.encode(this.color, buf);
    }

    private static TeleportTargetSymbol decode(PacketByteBuf buf) {
        char character = buf.readChar();
        var color = ColorRgb.PACKET_CODEC.decode(buf);
        return new TeleportTargetSymbol(character, color);
    }
}
