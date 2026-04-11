package yesman.epicfight.client.renderer.shader.compute.backend.ssbo;


import yesman.epicfight.client.renderer.shader.compute.backend.Sync;

import java.util.function.Supplier;

public class ArrayProxyPool {
    private final Supplier<IArrayBufferProxy> constructor;
    private final IArrayBufferProxy[] pool;
    private final Sync[] syncs;

    public ArrayProxyPool(Supplier<IArrayBufferProxy> constructor, int storage){
        this.constructor = constructor;
        pool = new IArrayBufferProxy[storage];
        syncs = new Sync[storage];
        for (int i = 0; i < storage; i++) {
            pool[i] = constructor.get();
            syncs[i] = new Sync();
        }
    }

    private int curr = 0;

    public IArrayBufferProxy getOrWait(){
        syncs[curr].setSync();
        for (int i = 0; i < pool.length; i++) {
            if(isFree(syncs[i])){
                curr = i;
                return pool[curr];
            }
        }
        curr = 0;
        //System.out.println("NF_:" + curr);
        waitSync(syncs[curr]);
        return pool[curr];
    }

    protected static void waitSync(Sync sync){
        if (!sync.isSyncSet()) {
            return;
        }

        if (!sync.isSyncSignaled()) {
            sync.waitSync();
        }

        sync.deleteSync();
        sync.resetSync();
    }

    protected static boolean isFree(Sync sync){
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
