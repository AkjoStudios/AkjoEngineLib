package com.akjostudios.engine.runtime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
@Accessors(chain = true, fluent = true)
public class AkjoEngineAppProperties {
    @JsonProperty(value = "appName")
    private String appName = "Untitled App";

    @JsonProperty(value = "appVersion")
    private String appVersion = "1.0.0";
}