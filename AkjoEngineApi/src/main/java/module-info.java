module AkjoEngineApi {
    requires org.jetbrains.annotations;
    requires static lombok;
    //noinspection Java9RedundantRequiresStatement
    requires org.slf4j;

    exports com.akjostudios.engine.api;
    exports com.akjostudios.engine.api.common;
    exports com.akjostudios.engine.api.common.cancel;
    exports com.akjostudios.engine.api.context;
    exports com.akjostudios.engine.api.logging;
    exports com.akjostudios.engine.api.lifecycle;
    exports com.akjostudios.engine.api.threading;
    exports com.akjostudios.engine.api.scheduling;
    exports com.akjostudios.engine.api.time;
    exports com.akjostudios.engine.api.event;
}