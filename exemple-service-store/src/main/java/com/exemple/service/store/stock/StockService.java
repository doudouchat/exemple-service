package com.exemple.service.store.stock;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.stock.distribution.StockDistribution;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Validated
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockDistribution distribution;

    private final StockResource resource;

    public Long update(@NotNull String company, @NotBlank String store, @NotBlank String product, int quantity) throws InsufficientStockException {

        long stock = get(company, store, product).orElse(0L);

        if (stock + quantity < 0) {
            throw new InsufficientStockException(company, store, product, stock, quantity);
        }

        LOG.debug("incrementation stock {} {} {} {}", company, store, product, quantity);

        resource.update(store, product, quantity);
        distribution.updateStock(company, store, product, stock + quantity);
        return stock + quantity;
    }

    public Optional<Long> get(@NotNull String company, @NotBlank String store, @NotBlank String product) {

        Optional<Long> stock = this.distribution.getStock(company, store, product);
        if (!stock.isPresent()) {
            return resource.get(store, product);
        }
        return stock;

    }

}
