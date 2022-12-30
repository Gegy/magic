package dev.gegy.magic.casting.spell;

import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.casting.blend.CastingBlendType;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.SpellCastingGlyph;
import dev.gegy.magic.client.glyph.spell.SpellGlyphs;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.glyph.GlyphForm;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import org.joml.Vector3f;

import java.util.List;

public final record SpellParameters(
        List<GlyphForm> glyphs,
        Vector3f direction
) {
    public static final PacketCodec<SpellParameters> CODEC = PacketCodec.of(SpellParameters::encode, SpellParameters::decode);

    public Spell blendOrCreate(
            PlayerEntity player, ClientCastingBuilder casting,
            SpellTransformType transform
    ) {
        var spell = casting.blendFrom(CastingBlendType.SPELL, transform);
        if (spell != null) {
            return spell;
        } else {
            return this.create(player, transform);
        }
    }

    public Spell create(PlayerEntity player, SpellTransformType transformType) {
        var source = SpellSource.of(player);

        var transform = transformType.create(source, this.direction, this.glyphs.size());
        var glyphs = new SpellGlyphs();

        var forms = this.glyphs;
        for (int i = 0; i < forms.size(); i++) {
            var form = forms.get(i);
            var glyphTransform = transform.getTransformForGlyph(i);
            glyphs.add(new SpellCastingGlyph(source, form, glyphTransform));
        }

        return new Spell(source, transform, glyphs);
    }

    private void encode(PacketByteBuf buf) {
        GlyphForm.PACKET_CODEC.list().encode(this.glyphs, buf);
        PacketCodec.VEC3F.encode(this.direction, buf);
    }

    private static SpellParameters decode(PacketByteBuf buf) {
        var glyphs = GlyphForm.PACKET_CODEC.list().decode(buf);
        var direction = PacketCodec.VEC3F.decode(buf);
        return new SpellParameters(glyphs, direction);
    }
}
