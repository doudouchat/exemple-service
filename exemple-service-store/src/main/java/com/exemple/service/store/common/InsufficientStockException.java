package com.exemple.service.store.common;

import java.text.MessageFormat;

import lombok.Getter;

@Getter
public class InsufficientStockException extends Exception {

    protected static final String EXCEPTION_MESSAGE = "Stock {0}:{1} is insufficient for quantity {2}";

    private static final long serialVersionUID = 1L;

    private final String company;

    private final String store;

    private final String product;

    private final long stock;

    private final long quantity;

    public InsufficientStockException(String company, String store, String product, long stock, long quantity) {
        super(MessageFormat.format(EXCEPTION_MESSAGE, company + store + product, stock, quantity));
        this.company = company;
        this.store = store;
        this.product = product;
        this.stock = stock;
        this.quantity = quantity;
    }

}
