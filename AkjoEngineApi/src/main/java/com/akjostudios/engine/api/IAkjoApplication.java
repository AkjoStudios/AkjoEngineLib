package com.akjostudios.engine.api;

@SuppressWarnings({"unused", "RedundantThrows", "EmptyMethod"})
public interface IAkjoApplication {
    void onInit() throws Exception;
    void onStart() throws Exception;
    void onUpdate(double deltaTime) throws Exception;
    void onPause() throws Exception;
    void onResume() throws Exception;
    void onStop() throws Exception;
    void onDestroy() throws Exception;
}