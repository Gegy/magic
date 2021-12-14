package dev.gegy.magic.client.spellcasting.draw;

import dev.gegy.magic.client.glyph.ClientGlyph;
import dev.gegy.magic.spellcasting.Spell;
import net.minecraft.client.network.ClientPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class SpellDrawController {
    private SpellDrawState state;

    public void tick(ClientPlayerEntity player) {
        SpellDrawState state = this.state;
        if (state == null) {
            this.state = state = new BeginDraw();
        }
        this.state = state.tick(player);
    }

    public void clear() {
        this.state = null;
    }

    @Nullable
    public ClientGlyph finishDrawingGlyph(Spell spell) {
        SpellDrawState state = this.state;
        if (state != null) {
            var result = state.finishDrawingGlyph(spell);
            this.state = result.state();
            return result.glyph();
        }
        return null;
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        SpellDrawState state = this.state;
        if (state != null) {
            return state.getDrawingGlyph();
        }
        return null;
    }
}
