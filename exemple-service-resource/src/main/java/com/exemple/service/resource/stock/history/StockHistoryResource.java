package com.exemple.service.resource.stock.history;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.stock.history.dao.StockHistoryDao;
import com.exemple.service.resource.stock.history.mapper.StockHistoryMapper;
import com.exemple.service.resource.stock.model.StockHistory;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StockHistoryResource {

    private static final String STOCK_HISTORY_TABLE = "stock_history";

    private final CqlSession session;

    private final ConcurrentMap<String, StockHistoryMapper> mappers = new ConcurrentHashMap<>();

    public SimpleStatement incrementQuantity(String store, String product, Instant date, String user, String application, long quantity) {

        return QueryBuilder.update(ResourceExecutionContext.get().keyspace(), STOCK_HISTORY_TABLE)
                .increment("quantity", QueryBuilder.literal(quantity))
                .whereColumn("store").isEqualTo(QueryBuilder.literal(store))
                .whereColumn("product").isEqualTo(QueryBuilder.literal(product))
                .whereColumn("date").isEqualTo(QueryBuilder.literal(date))
                .whereColumn("user").isEqualTo(QueryBuilder.literal(user))
                .whereColumn("application").isEqualTo(QueryBuilder.literal(application))
                .build();
    }

    public List<StockHistory> findByStoreAndProduct(String store, String product) {

        return dao().findByStoreAndProduct(store, product).all();
    }

    private StockHistoryDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::historyBuild).stockHistoryDao();
    }

    private StockHistoryMapper historyBuild(String keyspace) {

        return StockHistoryMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
