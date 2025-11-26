package yesman.epicfight.client.renderer.shader.compute.backend.utils;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

public class SimpleMemoryInterface {
    private final long offset;
    private final long size;

    public SimpleMemoryInterface(long offset, long size) {
        this.offset = offset;
        this.size = size;
    }

    public void putByte(long address, byte value) {
        MemoryUtil.memPutByte(address + offset, value);
    }

    
    public void putShort(long address, short value) {
        MemoryUtil.memPutShort(address + offset, value);
    }

    
    public void putInt(long address, int value) {
        MemoryUtil.memPutInt(address + offset, value);
    }

    
    public void putInt(long address, long value) {
        MemoryUtil.memPutInt(address + offset, (int) value);
    }

    
    public void putFloat(long address, float value) {
        MemoryUtil.memPutFloat(address + offset, value);
    }

    
    public void putNormal(long address, float value) {
        MemUtils.putNormal(address + offset, value);
    }

    
    public void putMatrix4f(long address, Matrix4f value) {
        MemUtils.putMatrix4f(address + offset, value);
    }

    
    public void putMatrix3f(long address, Matrix3f value) {
        MemUtils.putMatrix3f(address + offset, value);
    }

    public SimpleMemoryInterface at(int index) {
        return new SimpleMemoryInterface(index * size + offset, size);
    }
}
