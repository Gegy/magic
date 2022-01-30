package dev.gegy.magic.client.effect.casting.drawing;

import dev.gegy.magic.client.casting.drawing.ClientCastingDrawing;
import dev.gegy.magic.client.effect.Effect;
import dev.gegy.magic.client.effect.EffectType;
import dev.gegy.magic.client.casting.drawing.ClientDrawingGlyph;
import org.jetbrains.annotations.Nullable;

public final record DrawingEffect(ClientCastingDrawing casting) implements Effect {
    public static final EffectType<DrawingEffect> TYPE = EffectType.create();

    @Nullable
    public ClientDrawingGlyph getGlyph() {
        return this.casting.getDrawing();
    }

    @Override
    public EffectType<?> getType() {
        return TYPE;
    }
}
