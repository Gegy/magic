package dev.gegy.magic.client.effect.casting.spell.beam;

import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.math.ColorRgb;
import net.minecraft.util.math.MathHelper;

public final class BeamEffect implements Effect {
    public static final EffectType<BeamEffect> TYPE = EffectType.create();

    private final Spell spell;
    private final ColorRgb color = new ColorRgb(1.0F, 0.3F, 0.3F);

    private float prevLength;
    private float length;

    private final float scale = 0.5F;

    public BeamEffect(Spell spell) {
        this.spell = spell;
    }

    public void tick(float length) {
        this.prevLength = Math.min(length, this.length);
        this.length = length;
    }

    public Spell spell() {
        return this.spell;
    }

    public ColorRgb color() {
        return this.color;
    }

    public float getLength(float tickDelta) {
        return MathHelper.lerp(tickDelta, this.prevLength, this.length);
    }

    public float scale() {
        return this.scale;
    }

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }
}
