package dev.gegy.magic.client.glyph;

import dev.gegy.magic.client.glyph.transform.GlyphTransform;
import dev.gegy.magic.glyph.shape.GlyphEdge;
import dev.gegy.magic.glyph.shape.GlyphNode;
import dev.gegy.magic.math.Easings;
import dev.gegy.magic.spellcasting.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import org.jetbrains.annotations.Nullable;

public final class ClientGlyph {
    public static final float FORM_TICKS = 5.0F;

    private int networkId = -1;

    public final Entity source;
    public GlyphTransform transform;

    public final float radius;

    private final GlyphColor primaryColor = new GlyphColor(GlyphColor.DEFAULT_PRIMARY);
    private final GlyphColor secondaryColor = new GlyphColor(GlyphColor.DEFAULT_SECONDARY);

    public int shape;

    public GlyphStroke stroke;

    public final long createTime;

    private final Vec3f lookingAt = new Vec3f();

    private Vec3d lastLook;

    public ClientGlyph(Entity source, GlyphTransform transform, float radius, long createTime) {
        this.source = source;
        this.transform = transform;
        this.radius = radius;
        this.createTime = createTime;
    }

    public ClientGlyph(int networkId, Entity source, GlyphTransform transform, float radius, long createTime) {
        this(source, transform, radius, createTime);
        this.networkId = networkId;
    }

    public void setNetworkId(int networkId) {
        this.networkId = networkId;
    }

    public int getNetworkId() {
        return this.networkId;
    }

    public boolean hasNetworkId() {
        return this.networkId != -1;
    }

    public boolean tick() {
        this.primaryColor.tick(0.15F);
        this.secondaryColor.tick(0.15F);

        Vec3f lookingAt = this.lookingAt;
        this.computeLookingAt(lookingAt);

        GlyphStroke stroke = this.stroke;
        if (stroke != null) {
            float radius = this.radius;
            float x = Math.abs(lookingAt.getX() / radius);
            float y = lookingAt.getY() / radius;

            float distance2 = x * x + y * y;
            if (distance2 >= 1.0F) {
                float factor = MathHelper.fastInverseSqrt(distance2);
                x *= factor;
                y *= factor;
            }

            stroke.tick(x, y);
        }

        this.transform.tick();

        return this.source.isRemoved();
    }

    private void computeLookingAt(Vec3f lookingAt) {
        Vec3d look = this.source.getRotationVec(1.0F);
        if (look.equals(this.lastLook)) {
            return;
        }

        this.lastLook = look;

        lookingAt.set((float) look.x, (float) look.y, (float) look.z);
        this.transform.projectOntoPlane(lookingAt);
    }

    public boolean putEdge(GlyphEdge edge) {
        int bit = edge.asBit();
        if ((this.shape & bit) == 0) {
            this.shape |= bit;
            return true;
        } else {
            return false;
        }
    }

    public float getFormProgress(long time, float tickDelta) {
        float age = (float) (time - this.createTime) + tickDelta;
        float progress = Math.min(age / FORM_TICKS, 1.0F);
        return Easings.easeInCirc(progress);
    }

    public GlyphStroke startStroke(GlyphNode node) {
        Vec2f point = node.getPoint();
        GlyphStroke stroke = new GlyphStroke(point.x, point.y);
        this.stroke = stroke;
        return stroke;
    }

    public void stopStroke() {
        this.stroke = null;
    }

    public void applySpell(Spell spell) {
        this.primaryColor.set(spell.red, spell.green, spell.blue);
        this.secondaryColor.set(GlyphColor.primaryToSecondary(spell.red, spell.green, spell.blue));
        this.stroke = null;
    }

    public void applyStroke(@Nullable GlyphNode node) {
        if (node != null) {
            this.startStroke(node);
        } else {
            this.stopStroke();
        }
    }

    public GlyphColor getPrimaryColor() {
        return this.primaryColor;
    }

    public GlyphColor getSecondaryColor() {
        return this.secondaryColor;
    }

    public Vec3f getLookingAt() {
        return this.lookingAt;
    }
}
