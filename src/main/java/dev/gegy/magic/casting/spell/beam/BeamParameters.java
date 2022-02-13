package dev.gegy.magic.casting.spell.beam;

import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.PacketByteBuf;

public final record BeamParameters(
        SpellParameters spell,
        boolean active
) {
    public static final PacketCodec<BeamParameters> CODEC = PacketCodec.of(BeamParameters::encode, BeamParameters::decode);

    private void encode(PacketByteBuf buf) {
        SpellParameters.CODEC.encode(this.spell, buf);
        buf.writeBoolean(this.active);
    }

    private static BeamParameters decode(PacketByteBuf buf) {
        var spell = SpellParameters.CODEC.decode(buf);
        boolean active = buf.readBoolean();
        return new BeamParameters(spell, active);
    }
}
