package dev.gegy.magic.casting.drawing.event.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;

public final record DrawGlyphShapeC2SEvent(int shape) {
    public static final PacketCodec<DrawGlyphShapeC2SEvent> CODEC = PacketCodec.of(DrawGlyphShapeC2SEvent::encode, DrawGlyphShapeC2SEvent::decode);
    public static final CastingEventSpec<DrawGlyphShapeC2SEvent> SPEC = CastingEventSpec.of(Magic.identifier("glyph_shape"), CODEC);

    private void encode(FriendlyByteBuf buf) {
        buf.writeShort(this.shape);
    }

    private static DrawGlyphShapeC2SEvent decode(FriendlyByteBuf buf) {
        int shape = buf.readShort();
        return new DrawGlyphShapeC2SEvent(shape);
    }
}
