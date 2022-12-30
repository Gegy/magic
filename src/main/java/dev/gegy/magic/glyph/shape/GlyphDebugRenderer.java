package dev.gegy.magic.glyph.shape;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.world.phys.Vec2;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GlyphDebugRenderer {
    private static final int IMAGE_SIZE = 64;
    private static final int IMAGE_PADDING = 4;

    private static final int GLYPH_DIAMETER = IMAGE_SIZE - IMAGE_PADDING * 2;
    private static final int GLYPH_RADIUS = GLYPH_DIAMETER / 2;

    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 6;

    private static final int RENDER_PER_SIZE = 10;

    public static void main(final String[] args) throws IOException {
        final GlyphShapeGenerator generator = new GlyphShapeGenerator(MIN_SIZE, MAX_SIZE);
        final List<GlyphShape> all = generator.generateAll();

        final Byte2ObjectMap<List<GlyphShape>> bySize = new Byte2ObjectOpenHashMap<>();
        for (final GlyphShape glyph : all) {
            bySize.computeIfAbsent((byte) glyph.size, s -> new ArrayList<>()).add(glyph);
        }

        final Random random = new Random();
        for (int size = MIN_SIZE; size <= MAX_SIZE; size++) {
            final List<GlyphShape> glyphsBySize = bySize.get((byte) size);
            if (glyphsBySize == null || glyphsBySize.isEmpty()) {
                continue;
            }

            for (int i = 0; i < RENDER_PER_SIZE; i++) {
                if (glyphsBySize.isEmpty()) {
                    break;
                }

                final GlyphShape glyph = glyphsBySize.remove(random.nextInt(glyphsBySize.size()));

                final BufferedImage image = render(glyph);
                ImageIO.write(image, "png", new File("glyph_" + size + "_" + i + ".png"));
            }
        }
    }

    public static BufferedImage render(final GlyphShape glyph) {
        final BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = image.createGraphics();

        graphics.setColor(Color.RED);
        graphics.drawOval(IMAGE_PADDING, IMAGE_PADDING, GLYPH_DIAMETER, GLYPH_DIAMETER);

        graphics.setColor(Color.BLUE);
        for (final GlyphEdge edge : glyph.edges) {
            final Vec2 from = edge.from.getPoint();
            final Vec2 to = edge.to.getPoint();

            graphics.drawLine(
                    transformCoordinate(from.x), transformCoordinate(-from.y),
                    transformCoordinate(to.x), transformCoordinate(-to.y)
            );

            if (from.x != 0.0 || to.x != 0.0) {
                graphics.drawLine(
                        transformCoordinate(-from.x), transformCoordinate(-from.y),
                        transformCoordinate(-to.x), transformCoordinate(-to.y)
                );
            }
        }

        return image;
    }

    private static int transformCoordinate(final float coordinate) {
        return Math.round((coordinate + 1.0f) * GLYPH_RADIUS) + IMAGE_PADDING;
    }
}
