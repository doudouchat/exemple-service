package com.exemple.service.resource.stock;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.datastax.oss.driver.api.core.CqlSession;
import com.exemple.service.resource.core.ResourceExecutionContext;
import com.exemple.service.resource.stock.dao.StockDao;
import com.exemple.service.resource.stock.mapper.StockMapper;
import com.exemple.service.resource.stock.model.StockEntity;
import com.exemple.service.store.stock.StockResource;

import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class StockResourceImpl implements StockResource {

    private final CqlSession session;

    private final ConcurrentMap<String, StockMapper> mappers = new ConcurrentHashMap<>();

    @Override
    public void update(String store, String product, long quantity) {

        dao().incrementQuantity(store, product, quantity);
    }

    @Override
    public Optional<Long> get(String store, String product) {

        return dao().findByStoreAndProduct(store, product).map(StockEntity::getQuantity);
    }

    private StockDao dao() {

        return mappers.computeIfAbsent(ResourceExecutionContext.get().keyspace(), this::build).stockDao();
    }

    private StockMapper build(String keyspace) {

        return StockMapper.builder(session).withDefaultKeyspace(keyspace).build();
    }

}
