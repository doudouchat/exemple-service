package com.exemple.service.store.stock.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.stock.StockResource;
import com.exemple.service.store.stock.StockService;
import com.exemple.service.store.stock.distribution.StockDistribution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final StockDistribution distribution;

    private final StockResource resource;

    @Override
    public Long update(String company, String store, String product, int quantity) throws InsufficientStockException {

        long stock = get(company, store, product).orElse(0L);

        if (stock + quantity < 0) {
            throw new InsufficientStockException(company, store, product, stock, quantity);
        }

        LOG.debug("incrementation stock {} {} {} {}", company, store, product, quantity);

        resource.update(store, product, quantity);
        distribution.updateStock(company, store, product, stock + quantity);
        return stock + quantity;
    }

    @Override
    public Optional<Long> get(String company, String store, String product) {

        Optional<Long> stock = this.distribution.getStock(company, store, product);
        if (!stock.isPresent()) {
            return resource.get(store, product);
        }
        return stock;

    }

}
