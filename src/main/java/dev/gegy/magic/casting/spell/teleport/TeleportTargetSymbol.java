package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.math.ColorRgb;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public record TeleportTargetSymbol(
        char character,
        ColorRgb color
) {
    public static final PacketCodec<TeleportTargetSymbol> CODEC = PacketCodec.of(TeleportTargetSymbol::encode, TeleportTargetSymbol::decode);

    private void encode(final FriendlyByteBuf buf) {
        buf.writeChar(character);
        ColorRgb.PACKET_CODEC.encode(color, buf);
    }

    private static TeleportTargetSymbol decode(final FriendlyByteBuf buf) {
        final char character = buf.readChar();
        final ColorRgb color = ColorRgb.PACKET_CODEC.decode(buf);
        return new TeleportTargetSymbol(character, color);
    }
}
