package com.akjostudios.engine.runtime.tools;

import com.akjostudios.engine.runtime.AkjoEngineProjectProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.jakarta.JsonSchemaGenerator;

import java.nio.file.Files;
import java.nio.file.Path;

public class AkjoEngineAppPropertySchemaGen {
    static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected target directory path!");
        }
        Path out = Path.of(args[0], "config_schema.json");
        Files.createDirectories(out.getParent());

        ObjectMapper mapper = new ObjectMapper().registerModule(new Jdk8Module());
        JsonSchemaGenerator gen = new JsonSchemaGenerator(mapper);
        JsonSchema schema = gen.generateSchema(AkjoEngineProjectProperties.class);
        mapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), schema);
        System.out.println("Schema generated at: " + out.toAbsolutePath());
    }
}