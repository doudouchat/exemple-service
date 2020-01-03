package com.exemple.service.store.stock;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.store.common.InsufficientStockException;
import com.exemple.service.store.common.NoFoundStockException;

public interface StockService {

    Long update(@NotNull String company, @NotBlank String store, @NotBlank String product, int quantity) throws InsufficientStockException;

    Long get(@NotNull String company, @NotBlank String store, @NotBlank String product) throws NoFoundStockException;

}
