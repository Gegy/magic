package dev.gegy.magic.casting.spell;

import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Vector3f;

import java.util.List;

public record SpellParameters(
        List<GlyphForm> glyphs,
        Vector3f direction
) {
    public static final PacketCodec<SpellParameters> CODEC = PacketCodec.of(SpellParameters::encode, SpellParameters::decode);

    private void encode(final FriendlyByteBuf buf) {
        GlyphForm.PACKET_CODEC.list().encode(glyphs, buf);
        PacketCodec.VEC3F.encode(direction, buf);
    }

    private static SpellParameters decode(final FriendlyByteBuf buf) {
        final List<GlyphForm> glyphs = GlyphForm.PACKET_CODEC.list().decode(buf);
        final Vector3f direction = PacketCodec.VEC3F.decode(buf);
        return new SpellParameters(glyphs, direction);
    }
}
