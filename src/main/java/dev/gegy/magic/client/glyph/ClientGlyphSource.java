package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.animator.SpellcastingAnimator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ClientGlyphSource {
    private final Entity entity;

    private final SpellcastingAnimator animator = new SpellcastingAnimator();

    private ClientGlyph drawingGlyph;
    private List<ClientGlyph> preparedGlyphs;

    public ClientGlyphSource(Entity entity) {
        this.entity = entity;
    }

    void tick() {
        if (this.entity instanceof LivingEntity) {
            this.animator.tick((LivingEntity) this.entity);
        }
    }

    void setDrawingGlyph(ClientGlyph glyph) {
        this.drawingGlyph = glyph;
        if (glyph != null) {
            this.preparedGlyphs = null;
        }
    }

    void setPreparedGlyphs(List<ClientGlyph> glyphs) {
        this.drawingGlyph = null;
        this.preparedGlyphs = glyphs;
    }

    void removeGlyph(ClientGlyph glyph) {
        if (this.drawingGlyph == glyph) {
            this.drawingGlyph = null;
        }

        List<ClientGlyph> preparedGlyphs = this.preparedGlyphs;
        if (preparedGlyphs != null) {
            preparedGlyphs.remove(glyph);
        }
    }

    @Nullable
    public ClientGlyph getDrawingGlyph() {
        return this.drawingGlyph;
    }

    @Nullable
    public List<ClientGlyph> getPreparedGlyphs() {
        return this.preparedGlyphs;
    }

    public boolean isEmpty() {
        return this.drawingGlyph == null && this.preparedGlyphs == null;
    }

    public SpellcastingAnimator getAnimator() {
        return this.animator;
    }
}
