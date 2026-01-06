package yesman.epicfight.client.renderer.shader.compute.backend.pool;

import yesman.epicfight.client.renderer.shader.compute.backend.Sync;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.MappedBuffer;

public class BuffersPool {
    private MappedBuffer current;
    private final int currentId = 0;
    private final MappedBuffer[] buffersPool;
    private final Sync[] syncPool;
    private int size = 0;
    private final long bufferSize;

    public BuffersPool(int cap, long bufferSize) {
        this.buffersPool = new MappedBuffer[cap];
        this.syncPool = new Sync[cap];

        for (int i = 0; i < this.syncPool.length; i++) {
            this.syncPool[i] = new Sync();
        }

        this.bufferSize = bufferSize;
    }

    public MappedBuffer get(long space) {
        if (this.current == null) {
            this.current = new MappedBuffer(this.bufferSize);
            this.buffersPool[this.size++] = this.current;
            return this.current;
        }

        if (this.current.getSize() - this.current.getTail() >= space) { // current ok
            return this.current;
        } else { // small and mark using
            this.syncPool[currentId].setSync();
        }

        if (this.size < this.buffersPool.length) { // if not full, new one
            this.current = new MappedBuffer(this.bufferSize);
            this.buffersPool[size++] = this.current;

            return this.current;
        } else { // full try use old
            for (int i = 0; i < this.syncPool.length; i++) {
                var sync = this.syncPool[i];

                if (this.isFree(sync)) { // matched and return
                    this.current = this.buffersPool[i];
                    this.current.reset();
                    return this.current;
                }
            }
        }

        return null;
    }

    public MappedBuffer getOrWait(long space) {
        if (this.current == null) {
            this.current = new MappedBuffer(this.bufferSize);
            this.buffersPool[size++] = this.current;
            return this.current;
        }

        if (this.current.getSize() - this.current.getTail() >= space) { // current ok
            return this.current;
        } else { // small and mark using
            this.syncPool[this.currentId].setSync();
        }

        if (this.size < this.buffersPool.length) { // if not full, new one
            this.current = new MappedBuffer(this.bufferSize);
            this.buffersPool[this.size++] = this.current;
            return this.current;
        } else { // full try use old
            for (int i = 0; i < this.syncPool.length; i++) {
                var sync = this.syncPool[i];

                if (this.isFree(sync)) { // matched and return
                    this.current = this.buffersPool[i];
                    this.current.reset();
                    return this.current;
                }
            }

            this.current = this.buffersPool[0];
            this.waitSync(this.syncPool[0]);

            return this.current;
        }
    }

    private void waitSync(Sync sync) {
        if (!sync.isSyncSet()) {
            return;
        }

        if (!sync.isSyncSignaled()) {
            sync.waitSync();
        }

        sync.deleteSync();
        sync.resetSync();
    }

    private boolean isFree(Sync sync) {
        if (!sync.isSyncSet()) {
            return true;
        }

        if (!sync.isSyncSignaled()) {
            return false;
        }

        sync.deleteSync();
        sync.resetSync();

        return true;
    }
}
