package lab3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Semaphore {
    private int value = 0;

    public Semaphore(int value) {
        this.value = value;
    }

    public synchronized void P() {
        while (value <= 0) {
            try { wait(); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
        value--;
    }

    public synchronized void V() {
        value++;
        notify();
    }
}

class BufferSemaphore extends Buffer {
    Semaphore editSemaphore, freeSemaphore, fullSemaphore;

    public BufferSemaphore(int n) {
        super(n);
        editSemaphore = new Semaphore(1);
        freeSemaphore = new Semaphore(n);
        fullSemaphore = new Semaphore(0);
    }

    @Override
    public void put(int id, int n) {
        freeSemaphore.P();
        editSemaphore.P();

        buffer[(firstIndex + size) % bufferSize] = n;
        size++;
        System.out.println("Producer %d put: %d".formatted(id, n));


        fullSemaphore.V();
        editSemaphore.V();
    }

    @Override
    public int get(int id) {
        fullSemaphore.P();
        editSemaphore.P();

        int value = buffer[firstIndex];
        firstIndex = (firstIndex + 1 ) % bufferSize;
        size--;
        System.out.println("Consumer %d got: %d".formatted(id, value));

        freeSemaphore.V();
        editSemaphore.V();

        return value;
    }
}

public class PCSemaphore {
    private static void execute(int n1, int producerIterations, int n2, int consumerIterations) {
        BufferSemaphore bufferSemaphore = new BufferSemaphore(100);
        ExecutorService executorService = Executors.newFixedThreadPool(n1 + n2);
        for (int i = 0; i < n1; i++) {
            executorService.submit(new Producer(i, producerIterations, bufferSemaphore));
        }
        for (int i = 0 ; i < n2; i++) {
            executorService.submit(new Consumer(i, consumerIterations, bufferSemaphore));
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