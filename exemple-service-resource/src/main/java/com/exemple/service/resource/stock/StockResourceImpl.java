package com.exemple.service.resource.stock;

import static com.exemple.service.context.UserContext.USER_CONTEXT;
import static com.exemple.service.resource.common.ResourceContext.KEYSPACE;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BatchStatementBuilder;
import com.datastax.oss.driver.api.core.cql.BatchType;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.context.ServiceContextExecution;
import com.exemple.service.resource.stock.dao.StockDao;
import com.exemple.service.resource.stock.history.StockHistoryResource;
import com.exemple.service.resource.stock.mapper.StockMapper;
import com.exemple.service.resource.stock.model.StockEntity;
import com.exemple.service.store.stock.StockResource;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class StockResourceImpl implements StockResource {

    private static final String STOCK_TABLE = "stock";

    private final CqlSession session;

    private final StockHistoryResource stockHistory;

    private final ConcurrentMap<String, StockMapper> mappers = new ConcurrentHashMap<>();

    @Override
    public void update(String store, String product, long quantity) {

        var batch = new BatchStatementBuilder(BatchType.COUNTER)
                .addStatement(
                        QueryBuilder.update(KEYSPACE.get(), STOCK_TABLE)
                                .increment("quantity", QueryBuilder.literal(quantity))
                                .whereColumn("store").isEqualTo(QueryBuilder.literal(store))
                                .whereColumn("product").isEqualTo(QueryBuilder.literal(product))
                                .build())
                .addStatement(
                        stockHistory.incrementQuantity(store, product,
                                Instant.now(),
                                USER_CONTEXT.get().principal().getName(),
                                ServiceContextExecution.context().getApp(), quantity));

        session.execute(batch.build());
    }

    @Override
    public Optional<Long> get(String store, String product) {

        return dao().findByStoreAndProduct(store, product).map(StockEntity::getQuantity);
    }

    private StockDao dao() {

        return mappers.computeIfAbsent(KEYSPACE.get(), this::build).stockDao();
    }

    private StockMapper build(String keyspace) {

        return StockMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
