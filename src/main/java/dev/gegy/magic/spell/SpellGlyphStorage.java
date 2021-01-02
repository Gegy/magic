package dev.gegy.magic.spell;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.glyph.shape.GlyphShapeGenerator;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

public final class SpellGlyphStorage extends PersistentState {
    private static final String KEY = Magic.ID + ":spells";

    private final Int2ObjectMap<Spell> shapeToSpell = new Int2ObjectOpenHashMap<>();
    private boolean generated;

    private SpellGlyphStorage() {
        super(KEY);
    }

    public static SpellGlyphStorage get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(SpellGlyphStorage::new, KEY);
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

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        if (this.generated) {
            CompoundTag spellShapes = new CompoundTag();

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

    @Override
    public void fromTag(CompoundTag tag) {
        CompoundTag spellShapes = tag.getCompound("spell_shapes");

        for (String spellId : spellShapes.getKeys()) {
            Spell spell = Spell.REGISTRY.get(new Identifier(spellId));
            int shape = spellShapes.getInt(spellId);
            if (spell != null) {
                this.shapeToSpell.put(shape, spell);
            } else {
                Magic.LOGGER.warn("Invalid spell id: {}", spellId);
            }
        }
    }
}
