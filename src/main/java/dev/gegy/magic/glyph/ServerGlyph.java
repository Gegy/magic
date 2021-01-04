package dev.gegy.magic.glyph;

import dev.gegy.magic.spell.Spell;
import dev.gegy.magic.spell.SpellGlyphStorage;
import net.minecraft.client.util.math.Vector3f;
import org.jetbrains.annotations.Nullable;

public final class ServerGlyph {
    private final int networkId;
    private final ServerGlyphSource source;
    private final Vector3f direction;
    private final float radius;

    private int shape;

    private Spell matchedSpell;

    ServerGlyph(int networkId, ServerGlyphSource source, Vector3f direction, float radius) {
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

    public Vector3f getDirection() {
        return this.direction;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public Spell tryMatchSpell(SpellGlyphStorage spellStorage) {
        Spell spell = spellStorage.matchSpell(this.shape);
        this.matchedSpell = spell;
        return spell;
    }

    public int getShape() {
        return this.shape;
    }

    @Nullable
    public Spell getMatchedSpell() {
        return this.matchedSpell;
    }
}
