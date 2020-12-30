package dev.gegy.magic.mixin;

import dev.gegy.magic.client.Matrix3fAccess;
import net.minecraft.util.math.Matrix3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Matrix3f.class)
public class Matrix3fMixin implements Matrix3fAccess {
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
}
