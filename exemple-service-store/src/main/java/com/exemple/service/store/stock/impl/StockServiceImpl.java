package com.exemple.service.store.stock.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.resource.stock.StockResource;
import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.common.NoFoundStockException;
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

        StockDistribution.LockStock<Long> updateStock = () -> {

            long stock = get(company, store, product);
            checkIfStockIsSufficient(company, store, product, stock, quantity);

            LOG.debug("incrementation stock {} {} {} {}", company, store, product, quantity);

            resource.update(store, product, quantity);
            distribution.updateStock(company, store, product, Longs.toByteArray(stock + quantity));
            return stock + quantity;
        };

        return this.distribution.lockStock(company, store, product, updateStock, InsufficientStockException.class);

    }

    @Override
    public Long get(String company, String store, String product) throws NoFoundStockException {

        byte[] data = this.distribution.getStock(company, store, product);
        if (data.length == 0) {
            return resource.get(store, product);
        }
        return Longs.fromByteArray(data);

    }

    private static void checkIfStockIsSufficient(String company, String store, String product, long stock, int quantity)
            throws InsufficientStockException {
        if (stock + quantity < 0) {
            throw new InsufficientStockException(company, store, product, stock, quantity);
        }
    }

}
