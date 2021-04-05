package dev.gegy.magic.glyph.shape;

import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectOpenHashMap;
import net.minecraft.util.math.Vec2f;

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

    public static void main(String[] args) throws IOException {
        GlyphShapeGenerator generator = new GlyphShapeGenerator(MIN_SIZE, MAX_SIZE);
        List<GlyphShape> all = generator.generateAll();

        Byte2ObjectMap<List<GlyphShape>> bySize = new Byte2ObjectOpenHashMap<>();
        for (GlyphShape glyph : all) {
            bySize.computeIfAbsent((byte) glyph.size, s -> new ArrayList<>()).add(glyph);
        }

        Random random = new Random();
        for (int size = MIN_SIZE; size <= MAX_SIZE; size++) {
            List<GlyphShape> glyphsBySize = bySize.get((byte) size);
            if (glyphsBySize == null || glyphsBySize.isEmpty()) {
                continue;
            }

            for (int i = 0; i < RENDER_PER_SIZE; i++) {
                if (glyphsBySize.isEmpty()) {
                    break;
                }

                GlyphShape glyph = glyphsBySize.remove(random.nextInt(glyphsBySize.size()));

                BufferedImage image = render(glyph);
                ImageIO.write(image, "png", new File("glyph_" + size + "_" + i + ".png"));
            }
        }
    }

    public static BufferedImage render(GlyphShape glyph) {
        BufferedImage image = new BufferedImage(IMAGE_SIZE, IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();

        graphics.setColor(Color.RED);
        graphics.drawOval(IMAGE_PADDING, IMAGE_PADDING, GLYPH_DIAMETER, GLYPH_DIAMETER);

        graphics.setColor(Color.BLUE);
        for (GlyphEdge edge : glyph.edges) {
            Vec2f[] fromPoints = edge.from.getMirroredPoints();
            Vec2f[] toPoints = edge.to.getMirroredPoints();
            for (Vec2f from : fromPoints) {
                for (Vec2f to : toPoints) {
                    if (Math.signum(from.x) != Math.signum(to.x)) {
                        continue;
                    }
                    graphics.drawLine(
                            transformCoordinate(from.x), transformCoordinate(-from.y),
                            transformCoordinate(to.x), transformCoordinate(-to.y)
                    );
                }
            }
        }

        return image;
    }

    private static int transformCoordinate(float coordinate) {
        return Math.round((coordinate + 1.0F) * GLYPH_RADIUS) + IMAGE_PADDING;
    }
}
