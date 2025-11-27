package yesman.epicfight.client.renderer.shader.compute.backend.pool;

import java.util.Arrays;
import java.util.function.Supplier;

public class FixedCircularQueue<T> {

    private final Object[] elements;
    private int front = 0;
    private int rear;
    private int size = 0;
    private final int capacity;

    public FixedCircularQueue(int capacity) {
        this.elements = new Object[capacity];
        this.capacity = capacity;
        rear = -1;
    }

    public FixedCircularQueue(int capacity, Supplier<T> init) {
        this(capacity);
        for (int i = 0; i < elements.length; i++) {
            elements[i]=init.get();
        }
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    public boolean isFull() {
        return (size == capacity);
    }

    public void enqueue(T element) {
        if (isFull()) {
            throw new RuntimeException("Queue Full");
        }
        rear = (rear + 1) % capacity;
        elements[rear] = element;
        size++;
    }

    public T dequeue() {
        if (isEmpty()) {
            throw new RuntimeException("Queue Empty");
        }
        T element = (T) elements[front];
        front = (front + 1) % capacity;
        size--;
        return element;
    }
}
