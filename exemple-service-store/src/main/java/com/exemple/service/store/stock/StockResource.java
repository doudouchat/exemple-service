package com.exemple.service.store.stock;

import java.util.Optional;

import jakarta.validation.constraints.NotBlank;

public interface StockResource {

    void update(@NotBlank String store, @NotBlank String product, long quantity);

    Optional<Long> get(@NotBlank String store, @NotBlank String product);

}
