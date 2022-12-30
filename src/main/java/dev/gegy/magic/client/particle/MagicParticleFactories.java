package dev.gegy.magic.client.particle;

import dev.gegy.magic.particle.MagicParticles;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;

public final class MagicParticleFactories {
    public static void register() {
        ParticleFactoryRegistry.getInstance().register(MagicParticles.SPARK, SparkParticle.Factory::new);
    }
}
