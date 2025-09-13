package com.akjostudios.engine.api.monitor;

import java.util.function.Supplier;

@FunctionalInterface
@SuppressWarnings({"unused", "UnusedReturnValue"})
public interface MonitorProvider extends Supplier<Monitor> {}