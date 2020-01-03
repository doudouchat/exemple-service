package com.exemple.service.schema.common;

import java.io.InputStream;

import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

public final class SchemaBuilder {

    private SchemaBuilder() {

    }

    public static Schema build(InputStream source) {

        SchemaLoader schemaLoader = SchemaLoader.builder().draftV7Support().schemaJson(new JSONObject(new JSONTokener(source))).build();

        return schemaLoader.load().build();
    }

}
