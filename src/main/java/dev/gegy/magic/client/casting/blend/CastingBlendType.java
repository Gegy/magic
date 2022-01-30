package dev.gegy.magic.client.casting.blend;

import dev.gegy.magic.client.glyph.spell.Spell;

public final class CastingBlendType<T> {
    public static final CastingBlendType<Spell> SPELL = CastingBlendType.create();

    private CastingBlendType() {
    }

    public static <T> CastingBlendType<T> create() {
        return new CastingBlendType<>();
    }
}
