package dev.gegy.magic.client.effect.casting.spell;

import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.effect.glyph.GlyphEffect;
import dev.gegy.magic.client.glyph.spell.Spell;

public final class SpellEffects {
    public static void attach(Spell spell, ClientCastingBuilder casting) {
        casting.attachEffect(new PreparedSpellEffect(spell));
        casting.attachEffect(GlyphEffect.spell(spell));
    }
}
