package lab2;

class SemaphoreIf extends Semaphore {
    private boolean _state = true;
    private int _waiting = 0;

    public SemaphoreIf() {}

    @Override
    public synchronized void P() {
        if (!_state) {
            _waiting++;
            try { wait(); }
            catch (InterruptedException e) {
                e.printStackTrace();
            };
            _waiting--;

        }
        _state = false;
    }

    @Override
    public synchronized void V() {
        if (_waiting > 0) {
            _state = true;
            notify();
        }
        else {_state = true;}
    }
}
