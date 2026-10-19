package lab2;

class CounterSemaphore {
    int value = 0;
    Semaphore mutex = new Semaphore();
    Semaphore block = new Semaphore();

    public CounterSemaphore(int value) {
        this.value = value;
        if (value == 0) {
            this.block.P();
        }
    }

    public synchronized void P() {
        block.P();
        mutex.P();
        value--;
        while (value > 0) {
            block.V();
        }
        mutex.V();
    }

    public synchronized void V() {
        mutex.P();
        value++;
        if (value == 1) {
            block.V();
        }
        mutex.V();
    }
}