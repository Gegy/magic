package dev.gegy.magic.client;

import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;

public interface Matrix4fAccess {
    static Matrix4f create(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        Matrix4f matrix = new Matrix4f();
        set(matrix, m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
        return matrix;
    }

    static Matrix4f create(Matrix3f from) {
        Matrix4f matrix = new Matrix4f();
        set(matrix, from);
        return matrix;
    }

    static void set(
            Matrix4f matrix,
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        ((Matrix4fAccess) (Object) matrix).set(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33);
    }

    static void set(Matrix4f matrix, Matrix4f from) {
        ((Matrix4fAccess) (Object) from).copyInto(matrix);
    }

    static void set(Matrix4f matrix, Matrix3f from) {
        ((Matrix3fAccess) (Object) from).copyInto(matrix);
    }

    static void translate(Matrix4f matrix, float x, float y, float z) {
        ((Matrix4fAccess) (Object) matrix).translate(x, y, z);
    }

    void set(
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    );

    void copyInto(Matrix4f into);

    void translate(float x, float y, float z);
}
