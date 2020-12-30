package dev.gegy.magic.glyph;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import dev.gegy.magic.Magic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class WorldGlyphStorage extends PersistentState {
    private static final String KEY = Magic.ID + ":glyphs";

    private Glyph testGlyph;

    private WorldGlyphStorage() {
        super(KEY);
        this.assignGlyphs();
    }

    public static WorldGlyphStorage get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(WorldGlyphStorage::new, KEY);
    }

    public Glyph getTestGlyph() {
        this.assignGlyphs();
        return this.testGlyph;
    }

    private void assignGlyphs() {
        if (this.testGlyph != null) {
            return;
        }

        GlyphGenerator generator = new GlyphGenerator(3, 3);
        List<Glyph> glyphs = generator.generateAll();
        this.testGlyph = glyphs.get(new Random().nextInt(glyphs.size()));
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (this.testGlyph != null) {
            Optional<Tag> glyphResult = Glyph.CODEC.encodeStart(NbtOps.INSTANCE, this.testGlyph).result();
            glyphResult.ifPresent(glyphTag -> {
                tag.put("test_glyph", glyphTag);
            });
        }

        return tag;
    }

    @Override
    public void fromTag(CompoundTag tag) {
        DataResult<Glyph> result = Glyph.CODEC.decode(NbtOps.INSTANCE, tag.getCompound("test_glyph")).map(Pair::getFirst);
        result.result().ifPresent(glyph -> {
            this.testGlyph = glyph;
        });
    }
}
