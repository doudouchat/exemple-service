package com.exemple.service.resource.common.util;

import static com.exemple.service.resource.common.ResourceContext.KEYSPACE;

import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;
import com.datastax.oss.driver.api.core.session.Session;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MetadataSchemaUtils {

    public static TableMetadata getTableMetadata(Session session, String table) {

        return session.getMetadata().getKeyspace(KEYSPACE.get()).orElseThrow(IllegalStateException::new).getTable(table)
                .orElseThrow(IllegalStateException::new);
    }

}
