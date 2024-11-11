package com.etherblood.luna.network.server;

import java.util.UUID;

public class StripedLock {

    private final Object[] locks;

    public StripedLock(int size) {
        locks = new Object[size];
        for (int i = 0; i < size; i++) {
            locks[i] = new Object();
        }
    }

    public Object getLock(UUID id) {
        return locks[(int) Long.remainderUnsigned(id.getLeastSignificantBits(), locks.length)];
    }

    public void runSynchronized(UUID id, Runnable runnable) {
        synchronized (getLock(id)) {
            runnable.run();
        }
    }
}
