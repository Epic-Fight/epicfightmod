package yesman.epicfight.client.renderer.shader.compute.backend.pool;

import yesman.epicfight.client.renderer.shader.compute.backend.buffers.MappedBuffer;

public class BuffersPool{
    private MappedBuffer current;
    private final int currentId = 0;
    private final MappedBuffer[] buffersPool;
    private int size = 0;
    private final long bufferSize;

    public BuffersPool(int cap, long bufferSize) {

        this.buffersPool = new MappedBuffer[cap];
        this.bufferSize = bufferSize;
    }

    public MappedBuffer getOrWait(long space) {
        if (this.current == null) {
            this.current = new MappedBuffer(this.bufferSize);
            this.buffersPool[size++] = this.current;
            return this.current;
        }

        if (this.current.getSize() - this.current.getTail() >= space) { // current ok
            return this.current;
        } else { // small and mark
            current.setSync();
        }

        if (this.size < this.buffersPool.length) { // if not full, new one
            this.current = new MappedBuffer(this.bufferSize);
            this.buffersPool[this.size++] = this.current;
            return this.current;
        } else { // full try use old
            for (int i = 0; i < this.buffersPool.length; i++) {
                var buffer = buffersPool[i];
                if (buffer.isSyncd()) { // matched and return
                    this.current = buffer;
                    this.current.reset();
                    return this.current;
                }
            }

            this.current = this.buffersPool[0];
            this.current.waitSync();
            return this.current;
        }
    }
}
