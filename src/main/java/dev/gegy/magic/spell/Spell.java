package dev.gegy.magic.spell;

public final class Spell {
    public static final Spell TEST = new Spell(0.0F, 1.0F, 1.0F);

    public final float red;
    public final float green;
    public final float blue;

    public Spell(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
