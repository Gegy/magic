package dev.gegy.magic.glyph;

import com.google.common.base.Preconditions;
import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.action.BeamCastingAction;
import dev.gegy.magic.glyph.action.CastingAction;
import dev.gegy.magic.glyph.action.TeleportCastingAction;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

// TODO: terminology- a spell is a combination of glyphs
public final class GlyphType {
    public static final SimpleRegistry<GlyphType> REGISTRY = FabricRegistryBuilder.createSimple(GlyphType.class, Magic.identifier("spell"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final GlyphType BEAM = register("beam", GlyphType.builder()
            .style(GlyphStyle.RED)
            .action(new BeamCastingAction())
    );
    public static final GlyphType TELEPORT = register("teleport", GlyphType.builder()
            .style(GlyphStyle.PURPLE)
            .action(new TeleportCastingAction())
    );

    private final GlyphStyle style;
    private final CastingAction action;

    private GlyphType(GlyphStyle style, CastingAction action) {
        this.style = style;
        this.action = action;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static GlyphType register(String id, Builder spell) {
        return Registry.register(REGISTRY, Magic.identifier(id), spell.build());
    }

    public GlyphStyle style() {
        return this.style;
    }

    public CastingAction action() {
        return this.action;
    }

    public static final class Builder {
        private GlyphStyle style;
        private CastingAction action;

        private Builder() {
        }

        public Builder style(GlyphStyle style) {
            this.style = style;
            return this;
        }

        public Builder action(CastingAction action) {
            this.action = action;
            return this;
        }

        public GlyphType build() {
            return new GlyphType(
                    Preconditions.checkNotNull(this.style, "style not set"),
                    Preconditions.checkNotNull(this.action, "action not set")
            );
        }
    }
}
