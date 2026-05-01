package yesman.epicfight.client.renderer.shader.compute.backend.pool;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.function.Supplier;

@SuppressWarnings("UnUsed")
public class AbstractObjectPool<T> {
    private final Object[] objects;
    private final HashMap<T, Integer> id_map = Maps.newHashMap();
    private int size = 0;
    private final int capacity;
    private final Supplier<T> object_constructor;
    private final FixedCircularQueue<Integer> freeObjects;

    protected AbstractObjectPool(int capacity, Supplier<T> objectConstructor) {
        object_constructor = objectConstructor;
        this.objects = new Object[capacity];
        this.capacity = capacity;
        this.freeObjects = new FixedCircularQueue<>(capacity, () -> 0);
    }

    @SuppressWarnings("unchecked")
    protected T get() {
        if (this.freeObjects.isEmpty()) {
            if (this.tryCreate()) {
                return (T) this.objects[this.freeObjects.dequeue()];
            } else return null;
        } else {
            return (T) this.objects[this.freeObjects.dequeue()];
        }
    }

    protected boolean tryCreate(){
        if (this.size >= this.capacity) return false;

        T new_obj = this.object_constructor.get();
        this.objects[this.size] = new_obj;
        this.id_map.put(new_obj, this.size);
        this.freeObjects.enqueue(this.size);
        ++this.size;

        return true;
    }
}
