package dev.gegy.magic.spellcasting;

import dev.gegy.magic.Magic;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

// TODO: terminology- is a spell a combination of glyphs? or does each glyph represent a spell on its own?
public final class Spell {
    public static final SimpleRegistry<Spell> REGISTRY = FabricRegistryBuilder.createSimple(Spell.class, Magic.identifier("spell"))
            .attribute(RegistryAttribute.SYNCED)
            .buildAndRegister();

    public static final Spell CYAN = Registry.register(REGISTRY, Magic.identifier("cyan"), Spell.ofColor(0x19E6FF));
    public static final Spell RED = Registry.register(REGISTRY, Magic.identifier("red"), Spell.ofColor(0xFF4C19));
    public static final Spell PURPLE = Registry.register(REGISTRY, Magic.identifier("purple"), Spell.ofColor(0xD419FF));
    public static final Spell GREEN = Registry.register(REGISTRY, Magic.identifier("green"), Spell.ofColor(0x3FFF19));

    public final float red;
    public final float green;
    public final float blue;

    private Spell(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public static Spell ofColor(float red, float green, float blue) {
        return new Spell(red, green, blue);
    }

    public static Spell ofColor(int packed) {
        int red = (packed >> 16) & 0xFF;
        int green = (packed >> 8) & 0xFF;
        int blue = packed & 0xFF;
        return new Spell(red / 255.0F, green / 255.0F, blue / 255.0F);
    }
}
