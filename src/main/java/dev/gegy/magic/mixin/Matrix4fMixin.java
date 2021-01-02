package dev.gegy.magic.mixin;

import dev.gegy.magic.math.Matrix4fAccess;
import net.minecraft.util.math.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix4f.class)
public class Matrix4fMixin implements Matrix4fAccess {
    @Shadow
    protected float a00, a01, a02, a03;
    @Shadow
    protected float a10, a11, a12, a13;
    @Shadow
    protected float a20, a21, a22, a23;
    @Shadow
    protected float a30, a31, a32, a33;

    @Override
    public void set(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        this.a00 = m00;
        this.a01 = m01;
        this.a02 = m02;
        this.a03 = m03;
        this.a10 = m10;
        this.a11 = m11;
        this.a12 = m12;
        this.a13 = m13;
        this.a20 = m20;
        this.a21 = m21;
        this.a22 = m22;
        this.a23 = m23;
        this.a30 = m30;
        this.a31 = m31;
        this.a32 = m32;
        this.a33 = m33;
    }

    @Override
    public void copyInto(Matrix4f into) {
        Matrix4fAccess.set(into,
                this.a00, this.a01, this.a02, this.a03,
                this.a10, this.a11, this.a12, this.a13,
                this.a20, this.a21, this.a22, this.a23,
                this.a30, this.a31, this.a32, this.a33
        );
    }

    @Override
    public void translate(float x, float y, float z) {
        this.a03 += this.a00 * x + this.a01 * y + this.a02 * z;
        this.a13 += this.a10 * x + this.a11 * y + this.a12 * z;
        this.a23 += this.a20 * x + this.a21 * y + this.a22 * z;
        this.a33 += this.a30 * x + this.a31 * y + this.a32 * z;
    }
}
