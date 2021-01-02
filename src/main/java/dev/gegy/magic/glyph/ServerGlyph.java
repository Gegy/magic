package dev.gegy.magic.glyph;

import dev.gegy.magic.spell.Spell;
import dev.gegy.magic.spell.SpellStorage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class ServerGlyph {
    private final int networkId;
    private final ServerPlayerEntity source;
    private final GlyphPlane plane;
    private final float radius;

    private int shape;

    private Spell matchedSpell;

    ServerGlyph(int networkId, ServerPlayerEntity source, GlyphPlane plane, float radius) {
        this.networkId = networkId;
        this.source = source;
        this.plane = plane;
        this.radius = radius;
    }

    public int getNetworkId() {
        return this.networkId;
    }

    public ServerPlayerEntity getSource() {
        return this.source;
    }

    public GlyphPlane getPlane() {
        return this.plane;
    }

    public float getRadius() {
        return this.radius;
    }

    public void setShape(int shape) {
        this.shape = shape;
    }

    public boolean tryMatchSpell() {
        SpellStorage spellStorage = SpellStorage.get(this.source.server);
        Spell spell = spellStorage.matchSpell(this.shape);
        if (spell != null) {
            this.matchedSpell = spell;
            return true;
        }
        return false;
    }

    public int getShape() {
        return this.shape;
    }

    @Nullable
    public Spell getMatchedSpell() {
        return this.matchedSpell;
    }
}
