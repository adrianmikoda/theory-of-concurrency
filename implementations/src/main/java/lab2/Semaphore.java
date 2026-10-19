package lab2;

class Semaphore {
    private boolean _state = true;
    private int _waiting = 0;

    public Semaphore() {}

    public synchronized void P() {
        while (!_state) {
             _waiting++;
             try { wait(); }
             catch (InterruptedException e) {
                 e.printStackTrace();
             };
            _waiting--;

        }
        _state = false;
    }
    
    public synchronized void V() {
        if (_waiting > 0) {
            _state = true;
            notify();
        }
        else {_state = true;}
    }
}   
