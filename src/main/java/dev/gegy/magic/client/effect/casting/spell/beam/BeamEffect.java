package dev.gegy.magic.client.effect.casting.spell.beam;

import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.glyph.spell.Spell;
import dev.gegy.magic.math.ColorRgb;
import net.minecraft.util.math.MathHelper;

public final class BeamEffect implements Effect {
    public static final EffectType<BeamEffect> TYPE = EffectType.create();

    private final Spell spell;
    private final ColorRgb color = ColorRgb.of(1.0F, 0.3F, 0.3F);

    private float prevLength;
    private float length;

    private boolean visible;

    public BeamEffect(Spell spell) {
        this.spell = spell;
    }

    public void tick(float length) {
        this.prevLength = Math.min(length, this.length);
        this.length = length;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
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

    public boolean visible() {
        return this.visible;
    }

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }
}
