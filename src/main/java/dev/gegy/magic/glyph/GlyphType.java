package dev.gegy.magic.glyph;

import com.google.common.base.Preconditions;
import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.ServerCastingBuilder;
import dev.gegy.magic.casting.spell.SpellParameters;
import dev.gegy.magic.casting.spell.beam.ServerCastingBeam;
import dev.gegy.magic.casting.spell.teleport.ServerCastingTeleport;
import dev.gegy.magic.network.codec.PacketCodec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public final class GlyphType {
    public static final MappedRegistry<GlyphType> REGISTRY = FabricRegistryBuilder.createSimple(GlyphType.class, Magic.identifier("glyph_type"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final PacketCodec<@Nullable GlyphType> PACKET_CODEC = PacketCodec.ofRegistry(REGISTRY);

    public static final GlyphType BEAM = register("beam", GlyphType.builder()
            .style(GlyphStyle.RED)
            .casts(ServerCastingBeam::build)
    );
    public static final GlyphType TELEPORT = register("teleport", GlyphType.builder()
            .style(GlyphStyle.PURPLE)
            .casts(ServerCastingTeleport::build)
    );

    private final GlyphStyle style;
    private final CastFunction castFunction;

    private GlyphType(final GlyphStyle style, final CastFunction castFunction) {
        this.style = style;
        this.castFunction = castFunction;
    }

    public static void onInitialize() {
    }

    public static Builder builder() {
        return new Builder();
    }

    private static GlyphType register(final String id, final Builder glyph) {
        return Registry.register(REGISTRY, Magic.identifier(id), glyph.build());
    }

    public GlyphStyle style() {
        return style;
    }

    public CastFunction castFunction() {
        return castFunction;
    }

    public static final class Builder {
        private GlyphStyle style;
        private CastFunction castFunction;

        private Builder() {
        }

        public Builder style(final GlyphStyle style) {
            this.style = style;
            return this;
        }

        public Builder casts(final CastFunction castFunction) {
            this.castFunction = castFunction;
            return this;
        }

        public GlyphType build() {
            return new GlyphType(
                    Preconditions.checkNotNull(style, "style not set"),
                    Preconditions.checkNotNull(castFunction, "casting build function not set")
            );
        }
    }

    public interface CastFunction {
        ServerCasting build(ServerPlayer player, SpellParameters spell, ServerCastingBuilder casting);
    }
}
