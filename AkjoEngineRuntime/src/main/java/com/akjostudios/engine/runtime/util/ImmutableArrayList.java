package com.akjostudios.engine.runtime.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@SuppressWarnings({"unused", "UnusedReturnValue"})
public final class ImmutableArrayList<T> {
    private static final Object[] EMPTY = new Object[0];
    private final AtomicReference<Object[]> ref = new AtomicReference<>(EMPTY);

    public int size() { return ref.get().length; }

    public boolean add(T item) {
        for (;;) {
            Object[] current = ref.get();
            int len = current.length;
            Object[] next = new Object[len + 1];
            System.arraycopy(current, 0, next, 0, len);
            next[len] = item;
            if (ref.compareAndSet(current, next)) { return true; }
        }
    }

    public boolean remove(T item) {
        for (;;) {
            Object[] current = ref.get();
            int len = current.length;
            int index = indexOf(current, item);
            if (index < 0) { return false; }
            if (len == 1) {
                if (ref.compareAndSet(current, EMPTY)) { return true; }
                continue;
            }
            Object[] next = new Object[len - 1];
            if (index > 0) { System.arraycopy(current, 0, next, 0, index); }
            if (index < len - 1) { System.arraycopy(current, index + 1, next, index, len - index - 1); }
            if (ref.compareAndSet(current, next)) { return true; }
        }
    }

    @SuppressWarnings("unchecked")
    public void forEach(@NotNull Consumer<? super T> action) {
        Object[] snapshot = ref.get();
        for (Object item : snapshot) {
            action.accept((T) item);
        }
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public T[] snapshot(@NotNull Class<T> type) {
        Object[] snapshot = ref.get();
        T[] out = (T[]) Array.newInstance(type, snapshot.length);
        System.arraycopy(snapshot, 0, out, 0, snapshot.length);
        return out;
    }

    @Contract(pure = true)
    private int indexOf(Object@NotNull[] arr, T item) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == item) { return i; }
        }
        return -1;
    }
}