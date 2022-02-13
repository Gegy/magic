package dev.gegy.magic.casting.spell.teleport;

import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.PacketByteBuf;

import java.util.Map;
import java.util.UUID;

public final record TeleportParameters(
        SpellParameters spell,
        Map<UUID, TeleportTargetSymbol> targets
) {
    public static final PacketCodec<TeleportParameters> CODEC = PacketCodec.of(TeleportParameters::encode, TeleportParameters::decode);

    private static final PacketCodec<Map<UUID, TeleportTargetSymbol>> SYMBOL_MAP_CODEC = PacketCodec.mapOf(PacketCodec.UUID, TeleportTargetSymbol.CODEC);

    private void encode(PacketByteBuf buf) {
        SpellParameters.CODEC.encode(this.spell, buf);
        SYMBOL_MAP_CODEC.encode(this.targets, buf);
    }

    private static TeleportParameters decode(PacketByteBuf buf) {
        var spell = SpellParameters.CODEC.decode(buf);
        var symbols = SYMBOL_MAP_CODEC.decode(buf);
        return new TeleportParameters(spell, symbols);
    }
}
