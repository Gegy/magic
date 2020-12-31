package dev.gegy.magic.client.particle;

import dev.gegy.magic.Magic;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.registry.Registry;

public final class MagicParticles {
    public static final DefaultParticleType GLYPH_DRAW_TRAIL = FabricParticleTypes.simple();

    public static void onInitialize() {
        Registry.register(Registry.PARTICLE_TYPE, Magic.identifier("glyph_draw_trail"), GLYPH_DRAW_TRAIL);
    }

    public static void onInitializeClient() {
        ParticleFactoryRegistry.getInstance().register(GLYPH_DRAW_TRAIL, GlyphDrawTrailParticle.Factory::new);
    }
}
