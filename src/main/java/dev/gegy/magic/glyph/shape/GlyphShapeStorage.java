package dev.gegy.magic.glyph.shape;

import dev.gegy.magic.Magic;
import dev.gegy.magic.glyph.GlyphType;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Set;

public final class GlyphShapeStorage extends SavedData {
    private static final String KEY = Magic.ID + ":glyphs";
    private static final boolean EXPORT_SHAPES = "true".equals(System.getProperty(Magic.ID + ".debug.export_glyph_shapes"));

    private final Object2IntMap<GlyphType> glyphToShape = new Object2IntOpenHashMap<>();
    private final Int2ObjectMap<GlyphType> shapeToGlyph = new Int2ObjectOpenHashMap<>();
    private boolean generated;

    private GlyphShapeStorage() {
        this.glyphToShape.defaultReturnValue(-1);
    }

    public static GlyphShapeStorage get(MinecraftServer server) {
        DimensionDataStorage persistent = server.overworld().getDataStorage();
        return persistent.computeIfAbsent(GlyphShapeStorage::loadNbt, GlyphShapeStorage::new, KEY);
    }

    public int getShapeForGlyph(GlyphType glyph) {
        this.generateGlyphShapes();
        return this.glyphToShape.getInt(glyph);
    }

    @Nullable
    public GlyphType getGlyphForShape(int shape) {
        this.generateGlyphShapes();
        return this.shapeToGlyph.get(shape);
    }

    private void generateGlyphShapes() {
        if (this.generated) {
            return;
        }

        this.generated = true;

        Set<GlyphType> glyphsToGenerate = this.getGlyphsToGenerate();
        if (glyphsToGenerate.isEmpty()) {
            this.exportShapes();
            return;
        }

        GlyphShapeGenerator generator = new GlyphShapeGenerator(3, 3);
        List<GlyphShape> glyphs = generator.generateAll();

        SecureRandom random = new SecureRandom();
        for (GlyphType glyph : glyphsToGenerate) {
            GlyphShape shape;
            do {
                shape = glyphs.remove(random.nextInt(glyphs.size()));
            } while (!this.registerGlyphShape(glyph, shape.asBits()));
        }

        this.setDirty(true);

        this.exportShapes();
    }

    private boolean registerGlyphShape(GlyphType glyph, int shape) {
        return this.glyphToShape.putIfAbsent(glyph, shape) == -1
                && this.shapeToGlyph.putIfAbsent(shape, glyph) == null;
    }

    @NotNull
    private Set<GlyphType> getGlyphsToGenerate() {
        Set<GlyphType> glyphsToGenerate = new ReferenceOpenHashSet<>();
        for (GlyphType glyphType : GlyphType.REGISTRY) {
            glyphsToGenerate.add(glyphType);
        }
        glyphsToGenerate.removeAll(this.shapeToGlyph.values());

        return glyphsToGenerate;
    }

    private void exportShapes() {
        if (!EXPORT_SHAPES) {
            return;
        }

        for (Int2ObjectMap.Entry<GlyphType> entry : this.shapeToGlyph.int2ObjectEntrySet()) {
            GlyphType glyphType = entry.getValue();
            ResourceLocation glyphId = GlyphType.REGISTRY.getKey(glyphType);
            if (glyphId == null) {
                continue;
            }

            GlyphShape shape = new GlyphShape(entry.getIntKey());
            BufferedImage image = GlyphDebugRenderer.render(shape);

            try {
                ImageIO.write(image, "png", new File(glyphId.getPath() + ".png"));
            } catch (IOException e) {
                Magic.LOGGER.warn("Failed to export glyph shape", e);
            }
        }
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        if (this.generated) {
            CompoundTag glyphShapes = new CompoundTag();

            for (Int2ObjectMap.Entry<GlyphType> entry : this.shapeToGlyph.int2ObjectEntrySet()) {
                ResourceLocation glyphId = GlyphType.REGISTRY.getKey(entry.getValue());
                if (glyphId == null) {
                    continue;
                }

                glyphShapes.putInt(glyphId.toString(), entry.getIntKey());
            }

            tag.put("glyph_shapes", glyphShapes);
        }

        return tag;
    }

    private static GlyphShapeStorage loadNbt(CompoundTag tag) {
        GlyphShapeStorage storage = new GlyphShapeStorage();

        CompoundTag glyphShapes = tag.getCompound("glyph_shapes");

        for (String glyphId : glyphShapes.getAllKeys()) {
            GlyphType glyphType = GlyphType.REGISTRY.get(new ResourceLocation(glyphId));
            int shape = glyphShapes.getInt(glyphId);
            if (glyphType != null) {
                storage.registerGlyphShape(glyphType, shape);
            } else {
                Magic.LOGGER.warn("Invalid glyph type id: {}", glyphId);
            }
        }

        return storage;
    }
}
