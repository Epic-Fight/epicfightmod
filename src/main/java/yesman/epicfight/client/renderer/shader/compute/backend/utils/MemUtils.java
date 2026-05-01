package yesman.epicfight.client.renderer.shader.compute.backend.utils;

import net.minecraft.util.Mth;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class MemUtils {
    public static void putNormal(long address, float value) {
        MemoryUtil.memPutByte(address, (byte) ((int) (Mth.clamp(value, -1.0f, 1.0f) * 127.0f) & 0xFF));
    }

    public static void putMatrix3f(long address, Matrix3f matrix) {
        MemoryUtil.memPutFloat(address + 0L * 4L,	matrix.m00());
        MemoryUtil.memPutFloat(address + 1L * 4L,	matrix.m01());
        MemoryUtil.memPutFloat(address + 2L * 4L,	matrix.m02());

        MemoryUtil.memPutFloat(address + 4L * 4L,	matrix.m10());
        MemoryUtil.memPutFloat(address + 5L * 4L,	matrix.m11());
        MemoryUtil.memPutFloat(address + 6L * 4L,	matrix.m12());

        MemoryUtil.memPutFloat(address + 8L * 4L,	matrix.m20());
        MemoryUtil.memPutFloat(address + 9L * 4L,	matrix.m21());
        MemoryUtil.memPutFloat(address + 10L * 4L,	matrix.m22());
    }

    public static void putMatrix4f(long address, Matrix4f matrix) {
        MemoryUtil.memPutFloat(address + 0L * 4L,	matrix.m00());
        MemoryUtil.memPutFloat(address + 1L * 4L,	matrix.m01());
        MemoryUtil.memPutFloat(address + 2L * 4L,	matrix.m02());
        MemoryUtil.memPutFloat(address + 3L * 4L,	matrix.m03());

        MemoryUtil.memPutFloat(address + 4L * 4L,	matrix.m10());
        MemoryUtil.memPutFloat(address + 5L * 4L,	matrix.m11());
        MemoryUtil.memPutFloat(address + 6L * 4L,	matrix.m12());
        MemoryUtil.memPutFloat(address + 7L * 4L,	matrix.m13());

        MemoryUtil.memPutFloat(address + 8L * 4L,	matrix.m20());
        MemoryUtil.memPutFloat(address + 9L * 4L,	matrix.m21());
        MemoryUtil.memPutFloat(address + 10L * 4L,	matrix.m22());
        MemoryUtil.memPutFloat(address + 11L * 4L,	matrix.m23());

        MemoryUtil.memPutFloat(address + 12L * 4L,	matrix.m30());
        MemoryUtil.memPutFloat(address + 13L * 4L,	matrix.m31());
        MemoryUtil.memPutFloat(address + 14L * 4L,	matrix.m32());
        MemoryUtil.memPutFloat(address + 15L * 4L,	matrix.m33());
    }
}