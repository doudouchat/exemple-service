package com.exemple.service.store.stock;

import java.util.Optional;

import com.exemple.service.store.common.InsufficientStockException;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public interface StockService {

    Long update(@NotNull String company, @NotBlank String store, @NotBlank String product, int quantity) throws InsufficientStockException;

    Optional<Long> get(@NotNull String company, @NotBlank String store, @NotBlank String product);

}
