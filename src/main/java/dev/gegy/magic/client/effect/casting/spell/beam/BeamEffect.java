package dev.gegy.magic.client.effect.casting.spell.beam;

import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.math.ColorRgb;
import net.minecraft.util.Mth;

public final class BeamEffect implements Effect {
    public static final EffectType<BeamEffect> TYPE = EffectType.create();

    private final Spell spell;
    private final ColorRgb color = ColorRgb.of(1.0f, 0.3f, 0.3f);

    private float prevLength;
    private float length;

    private boolean visible;

    public BeamEffect(final Spell spell) {
        this.spell = spell;
    }

    public void tick(final float length) {
        prevLength = Math.min(length, this.length);
        this.length = length;
    }

    public void setVisible(final boolean visible) {
        this.visible = visible;
    }

    public Spell spell() {
        return spell;
    }

    public ColorRgb color() {
        return color;
    }

    public float getLength(final float tickDelta) {
        return Mth.lerp(tickDelta, prevLength, length);
    }

    public boolean visible() {
        return visible;
    }

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }
}
