package com.akjostudios.engine.api.common.mailbox;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

@SuppressWarnings("unused")
public final class Waiter {
    private final AtomicReference<Thread> parked = new AtomicReference<>(null);

    public void park(long nanos) {
        Thread current = Thread.currentThread();
        parked.set(current);
        LockSupport.parkNanos(nanos);
        parked.compareAndSet(current, null);
    }

    public void wake() {
        Thread parked = this.parked.getAndSet(null);
        if (parked != null) { LockSupport.unpark(parked); }
    }

    public boolean isParked() { return parked.get() != null; }
}