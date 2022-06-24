package com.exemple.service.api.launcher.core;

import java.io.File;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.github.nosan.embedded.cassandra.commons.FileSystemResource;
import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.ResourceCqlScript;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class InitCassandra {

    private final Resource[] scripts;

    private final CqlSession session;

    public InitCassandra(CqlSession session, @Value("${cassandra.embedded.scripts:}") String... scripts) {
        this.session = session;
        this.scripts = Arrays.stream(scripts).map(File::new).map(FileSystemResource::new).toArray(Resource[]::new);

    }

    @PostConstruct
    public void initKeyspace() {

        LOG.info("INIT EMBEDDED CASSANDRA");

        Arrays.stream(scripts).map(ResourceCqlScript::new).forEach((CqlScript script) -> script.forEachStatement(session::execute));

    }

}
