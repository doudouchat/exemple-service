package com.exemple.service.store.common;

import java.text.MessageFormat;

public class NoFoundStockException extends Exception {

    protected static final String EXCEPTION_MESSAGE = "Stock {0} is not found";

    private static final long serialVersionUID = 1L;

    private final String store;

    private final String product;

    public NoFoundStockException(String store, String product, Throwable cause) {
        super(MessageFormat.format(EXCEPTION_MESSAGE, store + product), cause);
        this.store = store;
        this.product = product;

    }

    public String getStore() {
        return store;
    }

    public String getProduct() {
        return product;
    }

}
