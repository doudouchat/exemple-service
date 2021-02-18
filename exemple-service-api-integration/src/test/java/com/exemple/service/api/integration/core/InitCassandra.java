package com.exemple.service.api.integration.core;

import java.io.File;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.github.nosan.embedded.cassandra.commons.FileSystemResource;
import com.github.nosan.embedded.cassandra.commons.Resource;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import com.github.nosan.embedded.cassandra.cql.ResourceCqlScript;

@Component
public class InitCassandra {

    private static final Logger LOG = LoggerFactory.getLogger(InitCassandra.class);

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
