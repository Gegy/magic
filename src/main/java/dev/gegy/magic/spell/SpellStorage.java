package dev.gegy.magic.spell;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.glyph.shape.GlyphShapeGenerator;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public final class SpellStorage extends PersistentState {
    private static final String KEY = Magic.ID + ":spells";

    private GlyphShape testGlyph;

    private SpellStorage() {
        super(KEY);
        this.assignGlyphs();
    }

    public static SpellStorage get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(SpellStorage::new, KEY);
    }

    @Nullable
    public Spell matchSpell(int glyph) {
        GlyphShape testGlyph = this.getTestGlyph();
        if (testGlyph.asBits() == glyph) {
            return Spell.TEST;
        }
        return null;
    }

    public GlyphShape getTestGlyph() {
        this.assignGlyphs();
        return this.testGlyph;
    }

    private void assignGlyphs() {
        if (this.testGlyph != null) {
            return;
        }

        GlyphShapeGenerator generator = new GlyphShapeGenerator(3, 3);
        List<GlyphShape> glyphs = generator.generateAll();
        this.testGlyph = glyphs.get(new Random().nextInt(glyphs.size()));

        this.setDirty(true);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (this.testGlyph != null) {
            tag.putInt("test_glyph", this.testGlyph.asBits());
        }

        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        if (tag.contains("test_glyph", NbtType.INT)) {
            this.testGlyph = new GlyphShape(tag.getInt("test_glyph"));
        }
    }
}
