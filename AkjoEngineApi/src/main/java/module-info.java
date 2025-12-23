module AkjoEngineApi {
    requires org.jetbrains.annotations;
    requires static lombok;
    //noinspection Java9RedundantRequiresStatement
    requires org.slf4j;
    requires org.joml;

    exports com.akjostudios.engine.api;

    exports com.akjostudios.engine.api.common.mailbox;
    exports com.akjostudios.engine.api.common.cancel;
    exports com.akjostudios.engine.api.common.base;
    exports com.akjostudios.engine.api.common.base.position;
    exports com.akjostudios.engine.api.common.base.resolution;
    exports com.akjostudios.engine.api.common.base.scale;
    exports com.akjostudios.engine.api.common.base.size;
    exports com.akjostudios.engine.api.common.base.color;
    exports com.akjostudios.engine.api.common.base.area.screen;

    exports com.akjostudios.engine.api.context;
    exports com.akjostudios.engine.api.logging;
    exports com.akjostudios.engine.api.lifecycle;
    exports com.akjostudios.engine.api.threading;
    exports com.akjostudios.engine.api.scheduling;
    exports com.akjostudios.engine.api.time;
    exports com.akjostudios.engine.api.event;
    exports com.akjostudios.engine.api.resource.file;
    exports com.akjostudios.engine.api.resource.asset;
    exports com.akjostudios.engine.api.canvas;

    exports com.akjostudios.engine.api.monitor;
    exports com.akjostudios.engine.api.monitor.events;
    exports com.akjostudios.engine.api.window;
    exports com.akjostudios.engine.api.window.builder;
    exports com.akjostudios.engine.api.window.events;

    exports com.akjostudios.engine.api.assets;

    exports com.akjostudios.engine.res;
}