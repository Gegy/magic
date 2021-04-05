package dev.gegy.magic.math;

import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public interface Matrix3fAccess {
    static Matrix3f create(
            float m00, float m01, float m02,
            float m10, float m11, float m12,
            float m20, float m21, float m22
    ) {
        Matrix3f matrix = new Matrix3f();
        set(matrix, m00, m01, m02, m10, m11, m12, m20, m21, m22);
        return matrix;
    }

    static void set(
            Matrix3f matrix,
            float m00, float m01, float m02,
            float m10, float m11, float m12,
            float m20, float m21, float m22
    ) {
        ((Matrix3fAccess) (Object) matrix).set(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }

    void set(
            float m00, float m01, float m02,
            float m10, float m11, float m12,
            float m20, float m21, float m22
    );

    void copyInto(Matrix4f matrix);
}
