package dev.gegy.magic.glyph;

import dev.gegy.magic.spellcasting.ServerSpellcastingSource;
import dev.gegy.magic.spellcasting.Spell;
import dev.gegy.magic.spellcasting.SpellGlyphStorage;
import dev.gegy.magic.glyph.shape.GlyphNode;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public final class ServerGlyph {
    private final int networkId;
    private final ServerSpellcastingSource source;
    private final Vec3f direction;
    private final float radius;

    private int shape;

    private Spell matchedSpell;

    private GlyphNode stroke;

    public ServerGlyph(int networkId, ServerSpellcastingSource source, Vec3f direction, float radius) {
        this.networkId = networkId;
        this.source = source;
        this.direction = direction;
        this.radius = radius;
    }

    public int getNetworkId() {
        return this.networkId;
    }

    public ServerSpellcastingSource getSource() {
        return this.source;
    }

    public Vec3f getDirection() {
        return this.direction;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public void setStroke(@Nullable GlyphNode stroke) {
        this.stroke = stroke;
    }

    public Spell tryMatchSpell(SpellGlyphStorage spellStorage) {
        Spell spell = spellStorage.matchSpell(this.shape);
        this.matchedSpell = spell;
        this.stroke = null;
        return spell;
    }

    public int getShape() {
        return this.shape;
    }

    @Nullable
    public Spell getMatchedSpell() {
        return this.matchedSpell;
    }

    @Nullable
    public GlyphNode getStroke() {
        return this.stroke;
    }
}
