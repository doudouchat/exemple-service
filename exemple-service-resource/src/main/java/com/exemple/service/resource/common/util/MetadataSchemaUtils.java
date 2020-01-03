package com.exemple.service.resource.common.util;

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.session.Session;
import com.exemple.service.resource.core.ResourceExecutionContext;

public final class MetadataSchemaUtils {

    private MetadataSchemaUtils() {

    }

    public static TableMetadata getTableMetadata(Session session, String table) {

        return session.getMetadata().getKeyspace(ResourceExecutionContext.get().keyspace()).orElseThrow(IllegalStateException::new).getTable(table)
                .orElseThrow(IllegalStateException::new);
    }

}
