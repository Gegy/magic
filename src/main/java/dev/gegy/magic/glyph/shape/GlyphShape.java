package dev.gegy.magic.glyph.shape;

import com.google.common.collect.Iterators;
import dev.gegy.magic.network.codec.PacketCodec;

import java.util.Arrays;
import java.util.Iterator;

public record GlyphShape(int mask) implements Iterable<GlyphEdge> {
    private static final int ALL_EDGES = toMask(GlyphEdge.EDGES);
    private static final int CENTER_LINE = toMask(GlyphEdge.CENTER_LINE);

    public static final GlyphShape EMPTY = new GlyphShape(0);

    public static final PacketCodec<GlyphShape> PACKET_CODEC = PacketCodec.of(
            (shape, buf) -> buf.writeShort(shape.mask),
            buf -> new GlyphShape(buf.readShort())
    );

    public GlyphShape {
        mask &= ALL_EDGES;
    }

    public boolean contains(final GlyphEdge edge) {
        return (mask & edge.mask()) != 0;
    }

    public GlyphShape withEdge(final GlyphEdge edge) {
        return new GlyphShape(mask | edge.mask());
    }

    public int size() {
        final int edgeCount = Integer.bitCount(mask);
        if (!canSimplify(mask)) {
            return edgeCount;
        }

        // all the points along the center are colinear, and to give an accurate measurement of the "size" of a glyph,
        // the merged line across is more meaningful.
        int simplifiedSize = edgeCount;
        int mergedLength = 0;

        for (final GlyphEdge edge : GlyphEdge.CENTER_LINE) {
            if (contains(edge)) {
                mergedLength++;
                simplifiedSize--;
            } else {
                if (mergedLength > 0) {
                    mergedLength = 0;
                    simplifiedSize++;
                }
            }
        }

        if (mergedLength > 0) {
            simplifiedSize++;
        }

        return simplifiedSize;
    }

    private static boolean canSimplify(final int glyph) {
        // we can potentially simplify if we have 2+ edges in the center
        final int edgesInCenterLine = glyph & CENTER_LINE;
        return Integer.bitCount(edgesInCenterLine) >= 2;
    }

    @Override
    public Iterator<GlyphEdge> iterator() {
        return Iterators.filter(Iterators.forArray(GlyphEdge.EDGES), this::contains);
    }

    private static int toMask(final GlyphEdge[] edges) {
        return Arrays.stream(edges).mapToInt(GlyphEdge::mask).reduce(0, (a, b) -> a | b);
    }
}
