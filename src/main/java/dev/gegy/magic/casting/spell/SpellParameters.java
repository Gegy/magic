package dev.gegy.magic.casting.spell;

import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.casting.blend.CastingBlendType;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.SpellCastingGlyph;
import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransform;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.List;

public record SpellParameters(
        List<GlyphForm> glyphs,
        Vector3f direction
) {
    public static final PacketCodec<SpellParameters> CODEC = PacketCodec.of(SpellParameters::encode, SpellParameters::decode);

    public Spell blendOrCreate(
            final Player player, final ClientCastingBuilder casting,
            final SpellTransformType transform
    ) {
        final Spell spell = casting.blendFrom(CastingBlendType.SPELL, transform);
        if (spell != null) {
            return spell;
        } else {
            return create(player, transform);
        }
    }

    public Spell create(final Player player, final SpellTransformType transformType) {
        final SpellSource source = SpellSource.of(player);

        final SpellTransform transform = transformType.create(source, direction, glyphs.size());
        final SpellGlyphs glyphs = new SpellGlyphs();

        final List<GlyphForm> forms = this.glyphs;
        for (int i = 0; i < forms.size(); i++) {
            final GlyphForm form = forms.get(i);
            final GlyphTransform glyphTransform = transform.getTransformForGlyph(i);
            glyphs.add(new SpellCastingGlyph(source, form, glyphTransform));
        }

        return new Spell(source, transform, glyphs);
    }

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
