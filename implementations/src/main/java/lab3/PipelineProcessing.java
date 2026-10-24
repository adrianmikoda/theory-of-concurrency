package lab3;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class PipelineProcessing {
    private static final int BUFFER_SIZE = 100;
    private static final int PRODUCER_COUNT = 1;
    private static final int PRODUCER_ITERATIONS = 100;
    private static final int PROCESSOR_COUNT = 5;
    private static final int PROCESSOR_ITERATIONS = 100;
    private static final int CONSUMER_COUNT = 1;
    private static final int CONSUMER_ITERATIONS = 100;

    private static final int[] buffer = new int[BUFFER_SIZE];
    private static final Semaphore[] stage = new Semaphore[PROCESSOR_COUNT + 1];
    private static final Semaphore free = new Semaphore(BUFFER_SIZE);

    public static void main(String[] args) {
        for (int i = 0; i < stage.length; i++) {
            stage[i] = new Semaphore(0);
        }
        ExecutorService executorService =
                Executors.newFixedThreadPool(PRODUCER_COUNT + PROCESSOR_COUNT + CONSUMER_COUNT);

        for (int i = 0; i < PRODUCER_COUNT; i++) {
            executorService.submit(new Producer(i, PRODUCER_ITERATIONS));
        }
        for (int i = 0; i < PROCESSOR_COUNT; i++) {
            executorService.submit(new Processor(i, PROCESSOR_ITERATIONS));
        }
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            executorService.submit(new Consumer(i, CONSUMER_ITERATIONS));
        }
        executorService.shutdown();
    }

    static class Producer implements Runnable {
        private final int id, iterations;

        public Producer(int id, int iterations) {
            this.id = id;
            this.iterations = iterations;
        }

        public void run() {
            try {
                for (int i = 0; i < iterations; i++) {
                    free.acquire();
                    int slot = i % BUFFER_SIZE;
                    buffer[slot] = i;
                    System.out.println("Producer %d put: %d at %d".formatted(id, i, slot));
                    stage[0].release();
                    Thread.sleep((int) (Math.random() * 100));
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    static class Processor implements Runnable {
        private final int id, iterations;

        public Processor(int id, int iterations) {
            this.id = id;
            this.iterations = iterations;
        }

        public void run() {
            try {
                for (int i = 0; i < iterations; i++) {
                    stage[id].acquire();
                    int slot = i % BUFFER_SIZE;
                    buffer[slot] += 1;
                    System.out.println("Processor %d processed: %d -> %d at %d".formatted(id, i, buffer[slot], slot));
                    stage[id + 1].release();
                    Thread.sleep((int) (Math.random() * 100));
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    static class Consumer implements Runnable {
        private final int id, iterations;

        public Consumer(int id, int iterations) {
            this.id = id;
            this.iterations = iterations;
        }

        public void run() {
            try {
                for (int i = 0; i < iterations; i++) {
                    stage[PROCESSOR_COUNT].acquire();
                    int slot = i % BUFFER_SIZE;
                    System.out.println("Consumer %d got: %d from %d".formatted(id, buffer[slot], slot));
                    free.release();
                    Thread.sleep((int) (Math.random() * 100));
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }
}