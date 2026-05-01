package yesman.epicfight.client.renderer.shader.compute.backend.pool;

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
        this.rear = -1;
    }

    public FixedCircularQueue(int capacity, Supplier<T> init) {
        this(capacity);

        for (int i = 0; i < this.elements.length; i++) {
            this.elements[i] = init.get();
        }
    }

    public boolean isEmpty() {
        return (this.size == 0);
    }

    public boolean isFull() {
        return (this.size == this.capacity);
    }

    public void enqueue(T element) {
        if (this.isFull()) {
            throw new RuntimeException("Queue Full");
        }

        this.rear = (this.rear + 1) % this.capacity;
        this.elements[this.rear] = element;
        this.size++;
    }

    public T dequeue() {
        if (this.isEmpty()) {
            throw new RuntimeException("Queue Empty");
        }

        T element = (T)this.elements[this.front];
        this.front = (this.front + 1) % this.capacity;
        this.size--;

        return element;
    }
}
