package dev.gegy.magic.spellcasting;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.shape.GlyphDebugRenderer;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.glyph.shape.GlyphShapeGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

public final class SpellGlyphStorage extends PersistentState {
    private static final String KEY = Magic.ID + ":spells";
    private static final boolean EXPORT_SHAPES = false;

    private final Int2ObjectMap<Spell> shapeToSpell = new Int2ObjectOpenHashMap<>();
    private boolean generated;

    private SpellGlyphStorage() {
    }

    public static SpellGlyphStorage get(MinecraftServer server) {
        PersistentStateManager persistent = server.getOverworld().getPersistentStateManager();
        return persistent.getOrCreate(SpellGlyphStorage::loadNbt, SpellGlyphStorage::new, KEY);
    }

    @Nullable
    public Spell matchSpell(int glyph) {
        this.generateGlyphShapes();
        return this.shapeToSpell.get(glyph);
    }

    private void generateGlyphShapes() {
        if (this.generated) {
            return;
        }

        this.generated = true;

        Set<Spell> spellsToGenerate = this.getSpellsToGenerate();
        if (spellsToGenerate.isEmpty()) {
            this.exportShapes();
            return;
        }

        GlyphShapeGenerator generator = new GlyphShapeGenerator(3, 3);
        List<GlyphShape> glyphs = generator.generateAll();

        SecureRandom random = new SecureRandom();
        for (Spell spell : spellsToGenerate) {
            GlyphShape shape;
            do {
                shape = glyphs.remove(random.nextInt(glyphs.size()));
            } while (this.shapeToSpell.putIfAbsent(shape.asBits(), spell) != null);
        }

        this.setDirty(true);

        this.exportShapes();
    }

    @NotNull
    private Set<Spell> getSpellsToGenerate() {
        Set<Spell> spellsToAssign = new ReferenceOpenHashSet<>();
        for (Spell spell : Spell.REGISTRY) {
            spellsToAssign.add(spell);
        }
        spellsToAssign.removeAll(this.shapeToSpell.values());

        return spellsToAssign;
    }

    private void exportShapes() {
        if (!EXPORT_SHAPES) {
            return;
        }

        for (Int2ObjectMap.Entry<Spell> entry : this.shapeToSpell.int2ObjectEntrySet()) {
            Spell spell = entry.getValue();
            Identifier spellId = Spell.REGISTRY.getId(spell);
            if (spellId == null) {
                continue;
            }

            GlyphShape shape = new GlyphShape(entry.getIntKey());
            BufferedImage image = GlyphDebugRenderer.render(shape);

            try {
                ImageIO.write(image, "png", new File(spellId.getPath() + ".png"));
            } catch (IOException e) {
                Magic.LOGGER.warn("Failed to export glyph shape", e);
            }
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        if (this.generated) {
            NbtCompound spellShapes = new NbtCompound();

            for (Int2ObjectMap.Entry<Spell> entry : this.shapeToSpell.int2ObjectEntrySet()) {
                Identifier spellId = Spell.REGISTRY.getId(entry.getValue());
                if (spellId == null) {
                    continue;
                }

                spellShapes.putInt(spellId.toString(), entry.getIntKey());
            }

            tag.put("spell_shapes", spellShapes);
        }

        return tag;
    }

    private static SpellGlyphStorage loadNbt(NbtCompound tag) {
        SpellGlyphStorage storage = new SpellGlyphStorage();

        NbtCompound spellShapes = tag.getCompound("spell_shapes");

        for (String spellId : spellShapes.getKeys()) {
            Spell spell = Spell.REGISTRY.get(new Identifier(spellId));
            int shape = spellShapes.getInt(spellId);
            if (spell != null) {
                storage.shapeToSpell.put(shape, spell);
            } else {
                Magic.LOGGER.warn("Invalid spell id: {}", spellId);
            }
        }

        return storage;
    }
}
