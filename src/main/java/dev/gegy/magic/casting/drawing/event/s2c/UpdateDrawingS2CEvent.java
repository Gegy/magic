package dev.gegy.magic.casting.drawing.event.s2c;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.glyph.GlyphType;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

public final record UpdateDrawingS2CEvent(
        int shape,
        @Nullable GlyphNode stroke,
        @Nullable GlyphType formedGlyphType
) {
    public static final PacketCodec<UpdateDrawingS2CEvent> CODEC = PacketCodec.of(UpdateDrawingS2CEvent::encode, UpdateDrawingS2CEvent::decode);
    public static final CastingEventSpec<UpdateDrawingS2CEvent> SPEC = CastingEventSpec.of(Magic.identifier("update_drawing"), CODEC);

    private void encode(PacketByteBuf buf) {
        buf.writeShort(this.shape);
        GlyphNode.PACKET_CODEC.encode(this.stroke, buf);
        GlyphType.PACKET_CODEC.encode(this.formedGlyphType, buf);
    }

    private static UpdateDrawingS2CEvent decode(PacketByteBuf buf) {
        int shape = buf.readShort();
        var stroke = GlyphNode.PACKET_CODEC.decode(buf);
        var formedGlyphType = GlyphType.PACKET_CODEC.decode(buf);
        return new UpdateDrawingS2CEvent(shape, stroke, formedGlyphType);
    }
}
