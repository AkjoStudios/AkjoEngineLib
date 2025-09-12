package com.akjostudios.engine.api.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a component for dependency injection.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SuppressWarnings("unused")
public @interface Component {}