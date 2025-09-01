package com.akjostudios.engine.api.common.cancel;

@SuppressWarnings("unused")
public interface Cancellable {
    boolean cancel();
    boolean isCancelled();
}