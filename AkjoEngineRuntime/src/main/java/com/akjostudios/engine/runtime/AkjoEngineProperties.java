package com.akjostudios.engine.runtime;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "engine")
@Getter @Setter
public class AkjoEngineProperties {
    private String basePackage;
}