package com.exemple.service.resource.stock;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.ConsistencyLevel;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.store.stock.StockResource;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class StockResourceImpl implements StockResource {

    private static final String STOCK_TABLE = "stock";

    private final CqlSession session;

    @Override
    public void update(String store, String product, long quantity) {

        var update = QueryBuilder.update(ResourceExecutionContext.get().keyspace(), STOCK_TABLE)
                .increment("quantity", QueryBuilder.literal(quantity)).whereColumn("store").isEqualTo(QueryBuilder.literal(store))
                .whereColumn("product").isEqualTo(QueryBuilder.literal(product));

        session.execute(update.build());
    }

    @Override
    public Optional<Long> get(String store, String product) {

        var select = QueryBuilder.selectFrom(ResourceExecutionContext.get().keyspace(), STOCK_TABLE).column("quantity").whereColumn("store")
                .isEqualTo(QueryBuilder.literal(store)).whereColumn("product").isEqualTo(QueryBuilder.literal(product));

        var row = session.execute(select.build().setConsistencyLevel(ConsistencyLevel.QUORUM)).one();

        return Optional.ofNullable(row != null ? row.getLong(0) : null);
    }

}
