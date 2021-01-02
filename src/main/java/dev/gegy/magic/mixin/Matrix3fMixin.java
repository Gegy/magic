package dev.gegy.magic.mixin;

import dev.gegy.magic.math.Matrix3fAccess;
import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.FloatBuffer;

@Mixin(Matrix3f.class)
public final class Matrix3fMixin implements Matrix3fAccess {
    @Shadow
    protected float a00, a01, a02;
    @Shadow
    protected float a10, a11, a12;
    @Shadow
    protected float a20, a21, a22;

    @Override
    public void set(
            float m00, float m01, float m02,
            float m10, float m11, float m12,
            float m20, float m21, float m22
    ) {
        this.a00 = m00;
        this.a01 = m01;
        this.a02 = m02;
        this.a10 = m10;
        this.a11 = m11;
        this.a12 = m12;
        this.a20 = m20;
        this.a21 = m21;
        this.a22 = m22;
    }

    @Override
    public void writeToBuffer(FloatBuffer buffer) {
        buffer.put(this.a00).put(this.a10).put(this.a20);
        buffer.put(this.a01).put(this.a11).put(this.a21);
        buffer.put(this.a02).put(this.a12).put(this.a22);
    }

    @Override
    public void copyInto(Matrix4f matrix) {
        Matrix4fAccess.set(matrix,
                this.a00, this.a01, this.a02, 0.0F,
                this.a10, this.a11, this.a12, 0.0F,
                this.a20, this.a21, this.a22, 0.0F,
                0.0F, 0.0F, 0.0F, 1.0F
        );
    }
}
