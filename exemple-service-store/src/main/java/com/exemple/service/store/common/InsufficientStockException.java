package com.exemple.service.store.common;

import lombok.Getter;

@Getter
public class InsufficientStockException extends Exception {

    private final String store;

    private final String product;

    private final long stock;

    private final long quantity;

    public InsufficientStockException(String store, String product, long stock, long quantity) {
        super("Stock %s in %s:%s is insufficient for quantity %s".formatted(product, store, stock, quantity));
        this.store = store;
        this.product = product;
        this.stock = stock;
        this.quantity = quantity;
    }

}
