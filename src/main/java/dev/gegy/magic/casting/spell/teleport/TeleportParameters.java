package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Map;
import java.util.UUID;

public record TeleportParameters(
        SpellParameters spell,
        Map<UUID, TeleportTargetSymbol> targets
) {
    public static final PacketCodec<TeleportParameters> CODEC = PacketCodec.of(TeleportParameters::encode, TeleportParameters::decode);

    private static final PacketCodec<Map<UUID, TeleportTargetSymbol>> SYMBOL_MAP_CODEC = PacketCodec.mapOf(PacketCodec.UUID, TeleportTargetSymbol.CODEC);

    private void encode(final FriendlyByteBuf buf) {
        SpellParameters.CODEC.encode(spell, buf);
        SYMBOL_MAP_CODEC.encode(targets, buf);
    }

    private static TeleportParameters decode(final FriendlyByteBuf buf) {
        final SpellParameters spell = SpellParameters.CODEC.decode(buf);
        final Map<UUID, TeleportTargetSymbol> symbols = SYMBOL_MAP_CODEC.decode(buf);
        return new TeleportParameters(spell, symbols);
    }
}
