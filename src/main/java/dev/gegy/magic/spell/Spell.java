package dev.gegy.magic.spell;

import dev.gegy.magic.Magic;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

public final class Spell {
    public static final SimpleRegistry<Spell> REGISTRY = FabricRegistryBuilder.createSimple(Spell.class, Magic.identifier("spell"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final Spell CYAN = Registry.register(REGISTRY, Magic.identifier("cyan"), new Spell(0.0F, 1.0F, 1.0F));
    public static final Spell RED = Registry.register(REGISTRY, Magic.identifier("red"), new Spell(1.0F, 0.0F, 0.0F));

    public final float red;
    public final float green;
    public final float blue;

    public Spell(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
