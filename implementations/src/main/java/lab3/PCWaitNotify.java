package lab3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Producer implements Runnable {
    private final Buffer buffer;
    private final int id, iterations;

    public Producer(int id, int iterations, Buffer buffer) {
        this.id = id;
        this.iterations = iterations;
        this.buffer = buffer;
    }

    public void run() {
        for (int i = 0; i < iterations; ++i) {
            buffer.put(id, i);
            try { Thread.sleep(100); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}

class Consumer implements Runnable {
    private final Buffer buffer;
    private final int id, iterations;

    public Consumer(int id, int iterations, Buffer buffer) {
        this.id = id;
        this.iterations = iterations;
        this.buffer = buffer;
    }

    public void run() {
        for (int i = 0; i < iterations; ++i) {
            try { Thread.sleep(100); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            buffer.get(id);
        }
    }
}

class Buffer {
    protected final int[] buffer;
    protected final int bufferSize;
    protected int size, firstIndex;

    public Buffer(int n) {
        this.bufferSize = n;
        this.buffer = new int[n];
        this.size = 0;
        this.firstIndex = 0;
    }

    public synchronized void put(int id, int n) {
        while (size == bufferSize) {
            System.out.println("Producer %d waits".formatted(id));
            try { wait(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        buffer[(firstIndex + size++) % bufferSize] = n;
        System.out.println("Producer %d put: %d".formatted(id, n));
        notifyAll();
    }

    public synchronized int get(int id) {
        while (size == 0) {
            System.out.println("Consumer %d waits".formatted(id));
            try { wait(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        int value = buffer[firstIndex];
        firstIndex = (firstIndex + 1 ) % bufferSize;
        size--;
        System.out.println("Consumer %d got: %d".formatted(id, value));
        notifyAll();

        return value;
    }
}

public class PCWaitNotify {

    private static void execute(int n1, int producerIterations, int n2, int consumerIterations) {
        Buffer buffer = new Buffer(100);
        ExecutorService executorService = Executors.newFixedThreadPool(n1 + n2);
        for (int i = 0; i < n1; i++) {
            executorService.submit(new Producer(i, producerIterations, buffer));
        }
        for (int i = 0 ; i < n2; i++) {
            executorService.submit(new Consumer(i, consumerIterations, buffer));
        }
        executorService.shutdown();
    }

    public static void main(String[] args) {
//        execute(1, 100, 1, 100);
//        execute(2, 100, 2, 100);
//        execute(3, 100, 1, 300);
        execute(1, 300, 3, 100);
    }
}