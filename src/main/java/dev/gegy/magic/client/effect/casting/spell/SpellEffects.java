package dev.gegy.magic.client.effect.casting.spell;

import dev.gegy.magic.client.casting.ClientCastingBuilder;
import dev.gegy.magic.client.effect.glyph.GlyphsEffect;
import dev.gegy.magic.client.glyph.spell.Spell;

public final class SpellEffects {
    public static void attach(final Spell spell, final ClientCastingBuilder casting) {
        casting.attachEffect(new PreparedSpellEffect(spell));
        casting.attachEffect(GlyphsEffect.fromSpell(spell));
    }
}
