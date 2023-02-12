package com.exemple.service.api.launcher.core;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.datastax.oss.driver.api.core.CqlSession;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;

@Configuration
public class InitCassandra {

    private final Resource[] scripts;

    private final CqlSession session;

    public InitCassandra(CqlSession session, @Value("${cassandra.scripts:}") String... scripts) {
        this.session = session;
        this.scripts = Arrays.stream(scripts).map(File::new).map(FileSystemResource::new).toArray(Resource[]::new);

    }

    @PostConstruct
    public void initKeyspace() {

        this.session.setSchemaMetadataEnabled(false);
        Arrays.stream(scripts).flatMap((Resource script) -> Arrays.stream(splitScript(script))).forEach(session::execute);
        this.session.setSchemaMetadataEnabled(true);

    }

    @SneakyThrows
    private static String[] splitScript(Resource script) {
        return FileUtils.readFileToString(script.getFile(), StandardCharsets.UTF_8).trim().split(";");
    }

}
