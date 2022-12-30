package dev.gegy.magic.client.effect.casting.spell;

import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.spell.Spell;

public record PreparedSpellEffect(Spell spell) implements Effect {
    public static final EffectType<PreparedSpellEffect> TYPE = EffectType.create();

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }
}
