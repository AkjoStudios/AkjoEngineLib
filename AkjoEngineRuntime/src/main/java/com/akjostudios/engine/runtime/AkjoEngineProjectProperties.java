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
public class AkjoEngineProjectProperties {
    @JsonProperty(value = "name")
    private String projectName = "Untitled Project";

    @JsonProperty(value = "version")
    private String projectVersion = "1.0.0";
}