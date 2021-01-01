package dev.gegy.magic.glyph.shape;

import dev.gegy.magic.Magic;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.List;
import java.util.Random;

public final class GlyphShapeStorage extends PersistentState {
    private static final String KEY = Magic.ID + ":glyph_shapes";

    private GlyphShape testGlyph;

    private GlyphShapeStorage() {
        super(KEY);
        this.assignGlyphs();
    }

    public static GlyphShapeStorage get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(GlyphShapeStorage::new, KEY);
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
