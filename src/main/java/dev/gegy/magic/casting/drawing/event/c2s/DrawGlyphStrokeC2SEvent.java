package dev.gegy.magic.casting.drawing.event.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final record DrawGlyphStrokeC2SEvent(@Nullable GlyphNode node) {
    public static final PacketCodec<DrawGlyphStrokeC2SEvent> CODEC = PacketCodec.of(DrawGlyphStrokeC2SEvent::encode, DrawGlyphStrokeC2SEvent::decode);
    public static final CastingEventSpec<DrawGlyphStrokeC2SEvent> SPEC = CastingEventSpec.of(Magic.identifier("glyph_stroke"), CODEC);

    private static final DrawGlyphStrokeC2SEvent STOP = new DrawGlyphStrokeC2SEvent(null);

    public static DrawGlyphStrokeC2SEvent start(@NotNull GlyphNode node) {
        return new DrawGlyphStrokeC2SEvent(node);
    }

    public static DrawGlyphStrokeC2SEvent stop() {
        return STOP;
    }

    private void encode(FriendlyByteBuf buf) {
        GlyphNode.PACKET_CODEC.encode(this.node, buf);
    }

    private static DrawGlyphStrokeC2SEvent decode(FriendlyByteBuf buf) {
        var node = GlyphNode.PACKET_CODEC.decode(buf);
        return new DrawGlyphStrokeC2SEvent(node);
    }
}
