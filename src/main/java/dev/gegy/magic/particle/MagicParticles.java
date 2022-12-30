package dev.gegy.magic.particle;

import dev.gegy.magic.Magic;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;

public final class MagicParticles {
    public static final SimpleParticleType SPARK = FabricParticleTypes.simple();

    public static void register() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Magic.identifier("spark"), SPARK);
    }
}
