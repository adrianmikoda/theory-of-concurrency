package lab2;

import javax.swing.plaf.basic.BasicTreeUI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


class Counter {
    private int _val;
    public Counter(int n) {
        _val = n;
    }
    public void inc() {
        _val++;
    }
    public void dec() {
        _val--;
    }
    public int value() {
        return _val;
    }
}

class IThread extends Thread {
    Counter counter;
    int n;

    public IThread(Counter counter, int n) {
        this.counter = counter;
        this.n = n;
    }

    @Override
    public void run() {
        for (int i = 0; i < n; i++) {
            counter.inc();
        }
    }
}


class DThread extends Thread {
    Counter counter;
    int n;

    public DThread(Counter counter, int n) {
        this.counter = counter;
        this.n = n;
    }

    @Override
    public void run() {
        for (int i = 0; i < n; i++) {
            counter.dec();
        }
    }
}

class IThreadSemaphore extends IThread {
    Semaphore semaphore;

    public IThreadSemaphore(Counter counter, int n, Semaphore semaphore) {
        super(counter, n);
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        for (int i = 0; i < n; i++) {
            semaphore.P();
            counter.inc();
            semaphore.V();
        }
    }
}


class DThreadSemaphore extends DThread {
    Semaphore semaphore;

    public DThreadSemaphore(Counter counter, int n, Semaphore semaphore) {
        super(counter, n);
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        for (int i = 0; i < n; i++) {
            semaphore.P();
            counter.dec();
            semaphore.V();
        }
    }
}

public class Race {
    private static int getCounterValue(Counter counter, DThread dThread, IThread iThread) {
        dThread.start();
        iThread.start();
        try {
            dThread.join();
            iThread.join();
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted");
        }

        return counter.value();
    }

    public static void main(String[] args) throws InterruptedException {
        String path_without_synchronization = "../race_without_synchronization.txt";
        String path_semaphore_if = "../race_semaphore_if.txt";
        String path_semaphore_while = "../race_semaphore_while.txt";

        try {
            Files.deleteIfExists(Paths.get(path_without_synchronization));
            Files.deleteIfExists(Paths.get(path_semaphore_if));
            Files.deleteIfExists(Paths.get(path_semaphore_while));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 100; i++) {
            Counter counter = new Counter(0);
            DThread dThread = new DThread(counter, 10000);
            IThread iThread = new IThread(counter, 10000);
            String content = getCounterValue(counter, dThread, iThread) + System.lineSeparator();
            try {
                Files.write(Paths.get(path_without_synchronization), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {e.printStackTrace();}
        }

        for (int i = 0; i < 100; i++) {
            Counter counter = new Counter(0);
            SemaphoreIf semaphoreIf = new SemaphoreIf();
            DThreadSemaphore dThreadSemaphoreIf = new DThreadSemaphore(counter, 10000, semaphoreIf);
            IThreadSemaphore iThreadSemaphoreIf = new IThreadSemaphore(counter, 10000, semaphoreIf);
            String content = getCounterValue(counter, dThreadSemaphoreIf, iThreadSemaphoreIf) + System.lineSeparator();
            try {
                Files.write(Paths.get(path_semaphore_if), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {e.printStackTrace();}
        }

        for (int i = 0; i < 100; i++) {
            Counter counter = new Counter(0);
            Semaphore semaphore = new Semaphore();
            DThreadSemaphore dThreadSemaphore = new DThreadSemaphore(counter, 10000, semaphore);
            IThreadSemaphore iThreadSemaphore = new IThreadSemaphore(counter, 10000, semaphore);
            String content = getCounterValue(counter, dThreadSemaphore, iThreadSemaphore) + System.lineSeparator();
            try {
                Files.write(Paths.get(path_semaphore_while), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {e.printStackTrace();}
        }
    }
}