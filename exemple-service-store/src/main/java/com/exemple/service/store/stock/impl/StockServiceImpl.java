package com.exemple.service.store.stock.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.stock.StockResource;
import com.exemple.service.store.stock.StockService;
import com.exemple.service.store.stock.distribution.StockDistribution;
import com.google.common.primitives.Longs;

@Service
@Validated
public class StockServiceImpl implements StockService {

    private static final Logger LOG = LoggerFactory.getLogger(StockServiceImpl.class);

    private final StockDistribution distribution;

    private final StockResource resource;

    public StockServiceImpl(StockDistribution distribution, StockResource resource) {

        this.distribution = distribution;
        this.resource = resource;
    }

    @Override
    public Long update(String company, String store, String product, int quantity) throws InsufficientStockException {

        long stock = get(company, store, product).orElseThrow(IllegalArgumentException::new);

        if (stock + quantity < 0) {
            throw new InsufficientStockException(company, store, product, stock, quantity);
        }

        LOG.debug("incrementation stock {} {} {} {}", company, store, product, quantity);

        resource.update(store, product, quantity);
        distribution.updateStock(company, store, product, Longs.toByteArray(stock + quantity));
        return stock + quantity;
    }

    @Override
    public Optional<Long> get(String company, String store, String product) {

        return this.distribution.getStock(company, store, product).map((byte[] data) -> {
            if (data.length == 0) {
                return resource.get(store, product);
            }
            return Longs.fromByteArray(data);
        });

    }

}
