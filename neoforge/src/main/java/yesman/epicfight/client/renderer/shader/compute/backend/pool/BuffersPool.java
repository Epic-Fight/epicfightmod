package yesman.epicfight.client.renderer.shader.compute.backend.pool;

import yesman.epicfight.client.renderer.shader.compute.backend.Sync;
import yesman.epicfight.client.renderer.shader.compute.backend.buffers.MappedBuffer;

public class BuffersPool {

    private MappedBuffer current;
    private int current_id = 0;
    private final MappedBuffer[] buffers_pool;
    private final Sync[] sync_pool;
    private int size = 0;
    private final long buffer_size;

    public BuffersPool(int cap, long bufferSize){
        this.buffers_pool = new MappedBuffer[cap];
        sync_pool = new Sync[cap];
        for (int i = 0; i < sync_pool.length; i++) {
            sync_pool[i] = new Sync();
        }
        buffer_size = bufferSize;
    }

    public MappedBuffer get(long space){
        if (current == null){
            current = new MappedBuffer(buffer_size);
            buffers_pool[size++] = current;
            return current;
        }

        if (current.getSize() - current.getTail() >= space){ // current ok
            return current;
        }
        else { // small and mark using
            sync_pool[current_id].setSync();
        }

        if (size < buffers_pool.length){ // if not full, new one
            current = new MappedBuffer(buffer_size);
            buffers_pool[size++] = current;
            return current;
        }
        else { // full try use old
            for (int i = 0; i < sync_pool.length; i++) {
                var sync = sync_pool[i];

                if (isFree(sync)){ // matched and return
                    current = buffers_pool[i];
                    current.reset();
                    return current;
                }
            }
        }
        return null;
    }


    public MappedBuffer getOrWait(long space){
        if (current == null){
            current = new MappedBuffer(buffer_size);
            buffers_pool[size++] = current;
            return current;
        }

        if (current.getSize() - current.getTail() >= space){ // current ok
            return current;
        }
        else { // small and mark using
            sync_pool[current_id].setSync();
        }

        if (size < buffers_pool.length){ // if not full, new one
            current = new MappedBuffer(buffer_size);
            buffers_pool[size++] = current;
            return current;
        }
        else { // full try use old
            for (int i = 0; i < sync_pool.length; i++) {
                var sync = sync_pool[i];

                if (isFree(sync)){ // matched and return
                    current = buffers_pool[i];
                    current.reset();
                    return current;
                }
            }
            current = buffers_pool[0];
            waitSync(sync_pool[0]);
            return current;
        }
    }

    private void waitSync(Sync sync){
        if (!sync.isSyncSet()) {
            return;
        }

        if (!sync.isSyncSignaled()) {
            sync.waitSync();
        }

        sync.deleteSync	();
        sync.resetSync	();
    }

    private boolean isFree(Sync sync) {
        if (!sync.isSyncSet()) {
            return true;
        }

        if (!sync.isSyncSignaled()) {
            return false;
        }

        sync.deleteSync	();
        sync.resetSync	();

        return true;
    }

}
