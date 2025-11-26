package yesman.epicfight.client.renderer.shader.compute.backend.pool;

import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import java.util.HashMap;
import java.util.Queue;
import java.util.function.Supplier;

@SuppressWarnings("UnUsed")
public class AbstractObjectPool<T> {

    private final Object[] objects;
    private final HashMap<T, Integer> id_map = Maps.newHashMap();
    private int size = 0;
    private final int capacity;
    private final Supplier<T> object_constructor;
    private final FixedCircularQueue<Integer> free_objects;

    protected AbstractObjectPool(int capacity, Supplier<T> objectConstructor) {
        object_constructor = objectConstructor;
        this.objects = new Object[capacity];
        this.capacity = capacity;
        free_objects = new FixedCircularQueue<>(capacity, ()->0);
    }

    protected T get(){
        if(free_objects.isEmpty()){
            if(tryCreate()){
                return (T) objects[free_objects.dequeue()];
            }
            else return null;
        }
        else {
            return (T) objects[free_objects.dequeue()];
        }
    }

    protected boolean tryCreate(){
        if (size >= capacity) return false;
        T new_obj = object_constructor.get();
        objects[size] = new_obj;
        id_map.put(new_obj, size);
        free_objects.enqueue(size);
        ++size;
        return true;
    }


}
