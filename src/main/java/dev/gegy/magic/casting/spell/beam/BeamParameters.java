package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public record BeamParameters(
        SpellParameters spell,
        boolean active
) {
    public static final PacketCodec<BeamParameters> CODEC = PacketCodec.of(BeamParameters::encode, BeamParameters::decode);

    private void encode(final FriendlyByteBuf buf) {
        SpellParameters.CODEC.encode(spell, buf);
        buf.writeBoolean(active);
    }

    private static BeamParameters decode(final FriendlyByteBuf buf) {
        final SpellParameters spell = SpellParameters.CODEC.decode(buf);
        final boolean active = buf.readBoolean();
        return new BeamParameters(spell, active);
    }
}
