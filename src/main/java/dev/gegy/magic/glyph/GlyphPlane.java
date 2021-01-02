package dev.gegy.magic.glyph;

import dev.gegy.magic.math.Matrix3fAccess;
import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

// TODO: place glyph plane further away from source
public final class GlyphPlane {
    private final Vector3f direction;
    private final Matrix3f glyphToWorld;
    private final Matrix3f worldToGlyph;

    private Matrix4f renderGlyphToWorldMatrix;

    GlyphPlane(Vector3f direction, Matrix3f glyphToWorld, Matrix3f worldToGlyph) {
        this.direction = direction;
        this.glyphToWorld = glyphToWorld;
        this.worldToGlyph = worldToGlyph;
    }

    public static GlyphPlane createTowards(Vector3f direction) {
        Vector3f left = Vector3f.POSITIVE_Y.copy();
        left.cross(direction);
        left.normalize();

        Vector3f up = direction.copy();
        up.cross(left);
        up.normalize();

        Matrix3f glyphToWorld = Matrix3fAccess.create(
                left.getX(), up.getX(), direction.getX(),
                left.getY(), up.getY(), direction.getY(),
                left.getZ(), up.getZ(), direction.getZ()
        );

        Matrix3f worldToGlyph = glyphToWorld.copy();
        worldToGlyph.invert();

        return new GlyphPlane(direction, glyphToWorld, worldToGlyph);
    }

    public GlyphPlane centered(float x, float y) {
        Vector3f direction = new Vector3f(x, y, 1.0F);
        direction.transform(this.glyphToWorld);
        return createTowards(direction);
    }

    public void projectOntoPlane(Vector3f vector) {
        vector.transform(this.worldToGlyph);

        // once we're in plane space, move it onto the plane by scaling such that z=1
        vector.scale(1.0F / vector.getZ());
    }

    public Matrix4f getRenderGlyphToWorldMatrix() {
        Matrix4f renderMatrix = this.renderGlyphToWorldMatrix;
        if (renderMatrix == null) {
            this.renderGlyphToWorldMatrix = renderMatrix = Matrix4fAccess.create(this.glyphToWorld);
        }
        return renderMatrix;
    }

    public void writeTo(PacketByteBuf buf) {
        Vector3f direction = this.direction;
        buf.writeFloat(direction.getX());
        buf.writeFloat(direction.getY());
        buf.writeFloat(direction.getZ());
    }

    public static GlyphPlane readFrom(PacketByteBuf buf) {
        Vector3f direction = new Vector3f(buf.readFloat(), buf.readFloat(), buf.readFloat());
        return GlyphPlane.createTowards(direction);
    }
}
