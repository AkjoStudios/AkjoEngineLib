package com.akjostudios.engine.runtime.impl.time;

import com.akjostudios.engine.api.time.ThreadTime;
import com.akjostudios.engine.api.time.Time;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public final class TimeImpl implements Time {
    public static final double NANOS_PER_SECOND = 1_000_000_000.0;

    private final ThreadTimeImpl render = new  ThreadTimeImpl();
    private final ThreadTimeImpl logic = new ThreadTimeImpl();
    private final ThreadTimeImpl audio = new ThreadTimeImpl();

    private volatile double scale = 1.0;
    private volatile boolean paused = false;

    @Override
    public long nowNanos() { return System.nanoTime(); }

    @Override
    public double nowSeconds() { return nowNanos() / NANOS_PER_SECOND; }

    @Override
    public @NotNull ThreadTime render() { return render; }

    @Override
    public @NotNull ThreadTime logic() { return logic; }

    @Override
    public @NotNull ThreadTime audio() { return audio; }

    @Override
    public void setScale(double scale) { this.scale = Math.max(0.0, scale); }

    @Override
    public double getScale() { return scale; }

    @Override
    public void setPaused(boolean paused) { this.paused = paused; }

    @Override
    public boolean isPaused() { return paused; }

    public void publishRender(long nowNanos, double deltaTime) {
        render.publish(nowNanos, deltaTime, paused ? 0.0 : deltaTime * scale );
    }

    public void publishLogic(long nowNanos, double fixedDeltaTime) {
        logic.publish(nowNanos, fixedDeltaTime, paused ? 0.0 : fixedDeltaTime * scale );
    }

    public void publishAudio(long nowNanos, double deltaTime) {
        audio.publish(nowNanos, deltaTime, paused ? 0.0 : deltaTime * scale );
    }
}