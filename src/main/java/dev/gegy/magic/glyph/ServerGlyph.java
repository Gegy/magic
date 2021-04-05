package dev.gegy.magic.glyph;

import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.spell.Spell;
import dev.gegy.magic.spell.SpellGlyphStorage;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public final class ServerGlyph {
    private final int networkId;
    private final ServerGlyphSource source;
    private final Vec3f direction;
    private final float radius;

    private int shape;

    private Spell matchedSpell;

    private GlyphNode stroke;

    ServerGlyph(int networkId, ServerGlyphSource source, Vec3f direction, float radius) {
        this.networkId = networkId;
        this.source = source;
        this.direction = direction;
        this.radius = radius;
    }

    public int getNetworkId() {
        return this.networkId;
    }

    public ServerGlyphSource getSource() {
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
