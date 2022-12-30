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
        glyphToShape.defaultReturnValue(-1);
    }

    public static GlyphShapeStorage get(final MinecraftServer server) {
        final DimensionDataStorage persistent = server.overworld().getDataStorage();
        return persistent.computeIfAbsent(GlyphShapeStorage::loadNbt, GlyphShapeStorage::new, KEY);
    }

    public int getShapeForGlyph(final GlyphType glyph) {
        generateGlyphShapes();
        return glyphToShape.getInt(glyph);
    }

    @Nullable
    public GlyphType getGlyphForShape(final int shape) {
        generateGlyphShapes();
        return shapeToGlyph.get(shape);
    }

    private void generateGlyphShapes() {
        if (generated) {
            return;
        }

        generated = true;

        final Set<GlyphType> glyphsToGenerate = getGlyphsToGenerate();
        if (glyphsToGenerate.isEmpty()) {
            exportShapes();
            return;
        }

        final GlyphShapeGenerator generator = new GlyphShapeGenerator(3, 3);
        final List<GlyphShape> glyphs = generator.generateAll();

        final SecureRandom random = new SecureRandom();
        for (final GlyphType glyph : glyphsToGenerate) {
            GlyphShape shape;
            do {
                shape = glyphs.remove(random.nextInt(glyphs.size()));
            } while (!registerGlyphShape(glyph, shape.asBits()));
        }

        setDirty(true);

        exportShapes();
    }

    private boolean registerGlyphShape(final GlyphType glyph, final int shape) {
        return glyphToShape.putIfAbsent(glyph, shape) == -1
                && shapeToGlyph.putIfAbsent(shape, glyph) == null;
    }

    @NotNull
    private Set<GlyphType> getGlyphsToGenerate() {
        final Set<GlyphType> glyphsToGenerate = new ReferenceOpenHashSet<>();
        for (final GlyphType glyphType : GlyphType.REGISTRY) {
            glyphsToGenerate.add(glyphType);
        }
        glyphsToGenerate.removeAll(shapeToGlyph.values());

        return glyphsToGenerate;
    }

    private void exportShapes() {
        if (!EXPORT_SHAPES) {
            return;
        }

        for (final Int2ObjectMap.Entry<GlyphType> entry : shapeToGlyph.int2ObjectEntrySet()) {
            final GlyphType glyphType = entry.getValue();
            final ResourceLocation glyphId = GlyphType.REGISTRY.getKey(glyphType);
            if (glyphId == null) {
                continue;
            }

            final GlyphShape shape = new GlyphShape(entry.getIntKey());
            final BufferedImage image = GlyphDebugRenderer.render(shape);

            try {
                ImageIO.write(image, "png", new File(glyphId.getPath() + ".png"));
            } catch (final IOException e) {
                Magic.LOGGER.warn("Failed to export glyph shape", e);
            }
        }
    }

    @Override
    public CompoundTag save(final CompoundTag tag) {
        if (generated) {
            final CompoundTag glyphShapes = new CompoundTag();

            for (final Int2ObjectMap.Entry<GlyphType> entry : shapeToGlyph.int2ObjectEntrySet()) {
                final ResourceLocation glyphId = GlyphType.REGISTRY.getKey(entry.getValue());
                if (glyphId == null) {
                    continue;
                }

                glyphShapes.putInt(glyphId.toString(), entry.getIntKey());
            }

            tag.put("glyph_shapes", glyphShapes);
        }

        return tag;
    }

    private static GlyphShapeStorage loadNbt(final CompoundTag tag) {
        final GlyphShapeStorage storage = new GlyphShapeStorage();

        final CompoundTag glyphShapes = tag.getCompound("glyph_shapes");

        for (final String glyphId : glyphShapes.getAllKeys()) {
            final GlyphType glyphType = GlyphType.REGISTRY.get(new ResourceLocation(glyphId));
            final int shape = glyphShapes.getInt(glyphId);
            if (glyphType != null) {
                storage.registerGlyphShape(glyphType, shape);
            } else {
                Magic.LOGGER.warn("Invalid glyph type id: {}", glyphId);
            }
        }

        return storage;
    }
}
