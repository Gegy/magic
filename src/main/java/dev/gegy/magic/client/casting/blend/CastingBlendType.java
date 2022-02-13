package dev.gegy.magic.client.casting.blend;

import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.client.glyph.spell.transform.SpellTransformType;

public final class CastingBlendType<T, P> {
    public static final CastingBlendType<Spell, SpellTransformType> SPELL = CastingBlendType.create();

    private CastingBlendType() {
    }

    public static <T, P> CastingBlendType<T, P> create() {
        return new CastingBlendType<>();
    }
}
