package yesman.epicfight.client.renderer.shader.compute.backend.buffers;

import java.nio.ByteBuffer;

public class EmptyServerBuffer implements IServerBuffer {
    public static final EmptyServerBuffer INSTANCE = new EmptyServerBuffer();

    @Override
    public int getBufferHandle() {
        return 0;
    }

    @Override
    public void delete() {
    }

    @Override
    public void bind(int target) {
    }

    @Override
    public void data(ByteBuffer data) {
    }

    @Override
    public void bindBase(int target, int index) {
    }

    @Override
    public void bindRange(int target, int index, long offset, long size) {
    }
}
