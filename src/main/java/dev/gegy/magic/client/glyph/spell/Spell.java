package dev.gegy.magic.client.glyph.spell;

import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.casting.blend.CastingBlendType;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import dev.gegy.magic.client.glyph.SpellSource;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransform;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;
import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.GlyphForm;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public record Spell(
        SpellSource source,
        SpellTransform transform,
        SpellGlyphs glyphs
) {
    public static Spell blendOrCreate(final Player player, final ClientCastingBuilder casting, final SpellParameters parameters, final SpellTransformType transform) {
        final Spell spell = casting.blendFrom(CastingBlendType.SPELL, transform);
        if (spell != null) {
            return spell;
        } else {
            return create(player, parameters, transform);
        }
    }

    public static Spell create(final Player player, final SpellParameters parameters, final SpellTransformType transformType) {
        final SpellSource source = SpellSource.of(player);
        final SpellTransform transform = transformType.create(source, parameters.direction(), parameters.glyphs().size());
        return create(source, transform, parameters.glyphs());
    }

    private static Spell create(final SpellSource source, final SpellTransform transform, final List<GlyphForm> forms) {
        final SpellGlyphs glyphs = new SpellGlyphs();
        for (int i = 0; i < forms.size(); i++) {
            final GlyphForm form = forms.get(i);
            final GlyphTransform glyphTransform = transform.getTransformForGlyph(i);
            glyphs.add(new SpellCastingGlyph(source, form, glyphTransform));
        }
        return new Spell(source, transform, glyphs);
    }

    public static Spell prepare(final SpellSource source, final SpellTransform transform, final List<ClientDrawingGlyph> drawingGlyphs) {
        final List<GlyphForm> forms = drawingGlyphs.stream().map(ClientDrawingGlyph::asForm).toList();
        return create(source, transform, forms);
    }

    public void tick() {
        transform.tick();
    }
}
