package com.exemple.service.resource.stock.impl;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import com.datastax.oss.driver.api.querybuilder.update.Update;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.stock.StockResource;

@Service
@Validated
public class StockResourceImpl implements StockResource {

    private static final String STOCK_TABLE = "stock";

    private final CqlSession session;

    public StockResourceImpl(CqlSession session) {

        this.session = session;
    }

    @Override
    public void update(String store, String product, long quantity) {

        Update update = QueryBuilder.update(ResourceExecutionContext.get().keyspace(), STOCK_TABLE)
                .increment("quantity", QueryBuilder.literal(quantity)).whereColumn("store").isEqualTo(QueryBuilder.literal(store))
                .whereColumn("product").isEqualTo(QueryBuilder.literal(product));

        session.execute(update.build());
    }

    @Override
    public long get(String store, String product) {

        Select select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), STOCK_TABLE).column("quantity").whereColumn("store")
                .isEqualTo(QueryBuilder.literal(store)).whereColumn("product").isEqualTo(QueryBuilder.literal(product));

        Row row = session.execute(select.build().setConsistencyLevel(ConsistencyLevel.QUORUM)).one();

        return row != null ? row.getLong(0) : 0L;
    }

}
