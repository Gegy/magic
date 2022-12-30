package dev.gegy.magic.casting.drawing.event.c2s;

import dev.gegy.magic.Magic;
import dev.gegy.magic.casting.event.CastingEventSpec;
import dev.gegy.magic.network.codec.PacketCodec;
import net.minecraft.network.PacketByteBuf;
import org.joml.Vector3f;

public final record BeginGlyphC2SEvent(Vector3f direction, float radius) {
    public static final PacketCodec<BeginGlyphC2SEvent> CODEC = PacketCodec.of(BeginGlyphC2SEvent::encode, BeginGlyphC2SEvent::decode);
    public static final CastingEventSpec<BeginGlyphC2SEvent> SPEC = CastingEventSpec.of(Magic.identifier("begin_glyph"), CODEC);

    private void encode(PacketByteBuf buf) {
        PacketCodec.VEC3F.encode(this.direction, buf);
        buf.writeFloat(this.radius);
    }

    private static BeginGlyphC2SEvent decode(PacketByteBuf buf) {
        var direction = PacketCodec.VEC3F.decode(buf);
        float radius = buf.readFloat();
        return new BeginGlyphC2SEvent(direction, radius);
    }
}
