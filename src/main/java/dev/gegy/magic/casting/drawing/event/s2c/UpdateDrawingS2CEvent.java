package dev.gegy.magic.casting.drawing.event.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.glyph.shape.GlyphShape;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

public record UpdateDrawingS2CEvent(
        GlyphShape shape,
        @Nullable GlyphNode stroke,
        @Nullable GlyphType formedGlyphType
) {
    public static final PacketCodec<UpdateDrawingS2CEvent> CODEC = PacketCodec.of(UpdateDrawingS2CEvent::encode, UpdateDrawingS2CEvent::decode);
    public static final CastingEventSpec<UpdateDrawingS2CEvent> SPEC = CastingEventSpec.of(Magic.identifier("update_drawing"), CODEC);

    private void encode(final FriendlyByteBuf buf) {
        GlyphShape.PACKET_CODEC.encode(shape, buf);
        GlyphNode.PACKET_CODEC.encode(stroke, buf);
        GlyphType.PACKET_CODEC.encode(formedGlyphType, buf);
    }

    private static UpdateDrawingS2CEvent decode(final FriendlyByteBuf buf) {
        final GlyphShape shape = GlyphShape.PACKET_CODEC.decode(buf);
        final GlyphNode stroke = GlyphNode.PACKET_CODEC.decode(buf);
        final GlyphType formedGlyphType = GlyphType.PACKET_CODEC.decode(buf);
        return new UpdateDrawingS2CEvent(shape, stroke, formedGlyphType);
    }
}
