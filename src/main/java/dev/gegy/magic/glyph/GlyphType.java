package dev.gegy.magic.glyph;

import com.google.common.base.Preconditions;
import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.ServerCasting;
import dev.gegy.magic.casting.spell.beam.ServerCastingBeam;
import dev.gegy.magic.casting.spell.teleport.ServerCastingTeleport;
import dev.gegy.magic.network.codec.PacketCodec;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;

public final class GlyphType {
    public static final SimpleRegistry<GlyphType> REGISTRY = FabricRegistryBuilder.createSimple(GlyphType.class, Magic.identifier("glyph_type"))
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
    private final ServerCasting.Factory castFunction;

    private GlyphType(GlyphStyle style, ServerCasting.Factory castFunction) {
        this.style = style;
        this.castFunction = castFunction;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static GlyphType register(String id, Builder glyph) {
        return Registry.register(REGISTRY, Magic.identifier(id), glyph.build());
    }

    public GlyphStyle style() {
        return this.style;
    }

    public ServerCasting.Factory castFunction() {
        return this.castFunction;
    }

    public static final class Builder {
        private GlyphStyle style;
        private ServerCasting.Factory castFunction;

        private Builder() {
        }

        public Builder style(GlyphStyle style) {
            this.style = style;
            return this;
        }

        public Builder casts(ServerCasting.Factory castFunction) {
            this.castFunction = castFunction;
            return this;
        }

        public GlyphType build() {
            return new GlyphType(
                    Preconditions.checkNotNull(this.style, "style not set"),
                    Preconditions.checkNotNull(this.castFunction, "casting build function not set")
            );
        }
    }
}
