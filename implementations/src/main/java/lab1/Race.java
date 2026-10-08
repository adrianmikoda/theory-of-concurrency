package lab1;

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


class PetersonLock {
    volatile boolean want0 = false, want1 = false;
    volatile int turn;

    private boolean wantOther(int id) {
        return id == 0 ? want1 : want0;
    }

    public void lock(int id) {
        int other = 1 - id;
        if (id == 0) {want0 = true;} else {want1 = true;}
        turn = other;

        while (wantOther(id) && turn == other) {}
    }

    public void unlock(int id) {
        if (id == 0) {want0 = false;} else {want1 = false;};
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


class IThreadLock extends IThread {
    PetersonLock petersonLock;

    public IThreadLock(Counter counter, int n, PetersonLock petersonLock) {
        super(counter, n);
        this.petersonLock = petersonLock;
    }

    @Override
    public void run() {
        for (int i = 0; i < n; i++) {
            petersonLock.lock(0);
            counter.inc();
            petersonLock.unlock(0);
        }
    }
}


class DThreadLock extends DThread {
    PetersonLock petersonLock;

    public DThreadLock(Counter counter, int n, PetersonLock petersonLock) {
        super(counter, n);
        this.petersonLock = petersonLock;
    }

    @Override
    public void run() {
        for (int i = 0; i < n; i++) {
            petersonLock.lock(1);
            counter.dec();
            petersonLock.unlock(1);
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

    public static void main(String[] args) {
        String path_without_lock = "../race_without_lock.txt";
        String path_with_lock = "../race_with_lock.txt";

        try {
            Files.deleteIfExists(Paths.get(path_without_lock));
            Files.deleteIfExists(Paths.get(path_with_lock));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < 100; i++) {
            Counter counter = new Counter(0);
            DThread dThread = new DThread(counter, 10000);
            IThread iThread = new IThread(counter, 10000);
            String content = getCounterValue(counter, dThread, iThread) + System.lineSeparator();
            try {
                Files.write(Paths.get(path_without_lock), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {e.printStackTrace();}
        }

        for (int i = 0; i < 100; i++) {
            Counter counter = new Counter(0);
            PetersonLock petersonLock = new PetersonLock();
            DThreadLock dThreadLock = new DThreadLock(counter, 10000, petersonLock);
            IThreadLock iThreadLock = new IThreadLock(counter, 10000, petersonLock);

            String content = getCounterValue(counter, dThreadLock, iThreadLock) + System.lineSeparator();
            try {
                Files.write(Paths.get(path_with_lock), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {e.printStackTrace();}
        }
    }
}