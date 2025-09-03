package com.akjostudios.engine.runtime.impl.time;

import com.akjostudios.engine.api.time.ThreadTime;
import com.akjostudios.engine.runtime.util.MathUtil;
import lombok.RequiredArgsConstructor;

import static com.akjostudios.engine.runtime.impl.time.TimeImpl.NANOS_PER_SECOND;

@RequiredArgsConstructor
public final class ThreadTimeImpl implements ThreadTime {
    private volatile long index;
    private volatile long nowNanos;
    private volatile double nowSeconds;
    private volatile double deltaTime;
    private volatile double scaledDeltaTime;
    private volatile double averageDeltaTime;

    @Override
    public long index() { return index; }

    @Override
    public double deltaTime() { return deltaTime; }

    @Override
    public double scaledDeltaTime() { return scaledDeltaTime; }

    @Override
    public double averageDeltaTime() { return averageDeltaTime; }

    @Override
    public long lastNowNanos() { return nowNanos; }

    @Override
    public double lastNowSeconds() { return nowSeconds; }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    public void publish(long nowNanos, double deltaTime, double scaledDeltaTime) {
        this.nowNanos = nowNanos;
        this.nowSeconds = nowNanos / NANOS_PER_SECOND;
        this.deltaTime = deltaTime;
        this.scaledDeltaTime = scaledDeltaTime;

        this.averageDeltaTime = MathUtil.lerp(
                this.averageDeltaTime == 0.0 ? deltaTime : this.averageDeltaTime,
                deltaTime, 0.1
        );

        this.index++;
    }
}