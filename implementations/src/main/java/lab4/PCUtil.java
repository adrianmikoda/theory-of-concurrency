package lab4;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Arrays.stream;

class Buffer {
    protected final int[] buffer;
    protected final int bufferSize;
    protected int size = 0, firstIndex = 0;
    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();
    private final Random random = new Random();

    public Buffer(int bufferSize) {
        this.bufferSize = bufferSize;
        this.buffer = new int[bufferSize];
    }

    public void put(int n) throws InterruptedException {
        lock.lock();
        try {
            while (size + n > bufferSize) {
                notFull.await();
            }
            for (int i = 0; i < n; ++i) {
                buffer[(firstIndex + size++) % bufferSize] = random.nextInt();
            }
            notEmpty.signalAll();
        }
        finally {
            lock.unlock();
        }
    }

    public int[] get(int n) throws InterruptedException {
        lock.lock();
        int[] result = new int[n];
        try {
            while(size < n) {
                notEmpty.await();
            }
            for (int i = 0; i < n; ++i) {
                result[i] = buffer[firstIndex];
                firstIndex = (firstIndex + 1) % bufferSize;
                size--;
            }
            notFull.signalAll();
        }
        finally {
            lock.unlock();
        }

        return result;
    }
}

class Producer implements Runnable {
    private final int id, M;
    private final Buffer buffer;
    private final Random random = new Random();
    private final long[][] stats;

    public Producer(int id, int M, Buffer buffer, long[][] stats) {
        this.id = id;
        this.M = M;
        this.buffer = buffer;
        this.stats = stats;
    }

    public void run() {
        try {
            while (true) {
                int n = random.nextInt(M) + 1;
                long start = System.nanoTime();
                buffer.put(n);
                long end = System.nanoTime();
                stats[0][id] += end - start;
                stats[1][id]++;
                Thread.sleep(random.nextInt(150));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class Consumer implements Runnable {
    private final int id, M;
    private final Buffer buffer;
    private final Random random = new Random();
    private final long[][] stats;

    public Consumer(int id, int M, Buffer buffer, long[][] stats) {
        this.id = id;
        this.M = M;
        this.buffer = buffer;
        this.stats = stats;
    }

    public void run() {
        try {
            while (true) {
                int n = random.nextInt(M) + 1;
                long start = System.nanoTime();
                int[] result = buffer.get(n);
                long end = System.nanoTime();
                stats[0][id] += end - start;
                stats[1][id]++;
                stats[2][id] += n;
                Thread.sleep(random.nextInt(150));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

public class PCUtil {
    private static String test(int m, int n, int M) throws InterruptedException {
        final int MEASURE_MS = 5000;
        final int REPEATS = 5;

        double averageProducerWaitTime = 0;
        double averageConsumerWaitTime = 0;
        long throughput = 0;
        for (int i = 0; i < REPEATS; i++) {
            Buffer buffer = new Buffer(2*M);
            long[][] producerStats = new long[2][m];
            long[][] consumerStats = new long[3][n];
            ExecutorService executorService = Executors.newFixedThreadPool(m + n);
            for (int j = 0; j < m; j++) {
                executorService.submit(new Producer(j, M, buffer, producerStats));
            }
            for (int j = 0 ; j < n; j++) {
                executorService.submit(new Consumer(j, M, buffer, consumerStats));
            }
            executorService.shutdown();
            Thread.sleep(MEASURE_MS);
            executorService.shutdownNow();
            averageProducerWaitTime += (double) stream(producerStats[0]).sum() / (stream(producerStats[1]).sum() * 1000000000);
            averageConsumerWaitTime += (double) stream(consumerStats[0]).sum() / (stream(consumerStats[1]).sum() * 1000000000);
            throughput += stream(consumerStats[2]).sum();
        }
        averageProducerWaitTime /= REPEATS;
        averageConsumerWaitTime /= REPEATS;

        return "%d,%d,%d,%f,%f,%d".formatted(m,n,M,averageProducerWaitTime,averageConsumerWaitTime,throughput);
    }

    public static void main(String[] args) {
        String path = "../results.csv";
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] Ms = {100, 500, 1000, 5000};
        int[] workerSizes = {1, 5, 10, 50, 100, 500};
        String content = "m,n,M,avg_producer_wait_time,avg_consumer_wait_time,throughput" + System.lineSeparator();
        try {
            Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {e.printStackTrace();}
        try {
            for (int M : Ms) {
                for (int m : workerSizes) {
                    for (int n : workerSizes) {
                        content = test(m, n, M) + System.lineSeparator();
                        try {
                            Files.write(Paths.get(path), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } catch (Exception e) {e.printStackTrace();}
                    }
                }
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
