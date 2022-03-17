package com.exemple.service.store.stock;

import java.util.Optional;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.exemple.service.store.common.InsufficientStockException;

public interface StockService {

    Long update(@NotNull String company, @NotBlank String store, @NotBlank String product, int quantity) throws InsufficientStockException;

    Optional<Long> get(@NotNull String company, @NotBlank String store, @NotBlank String product);

}
