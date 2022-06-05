package com.exemple.service.resource.common.history;

import java.util.function.Supplier;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;

import lombok.SneakyThrows;

@FunctionalInterface
public interface HistoryDao<I, T extends HistoryModel<I>> extends Supplier<Object> {

    default BoundStatement save(T historyModel) {
        return invoke(get(), "save", historyModel);
    }

    default PagingIterable<T> findById(I id) {
        return invoke(get(), "findById", id);
    }

    default BoundStatement deleteByIdAndField(I id, String field) {
        return invoke(get(), "deleteByIdAndField", id, field);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    static <T> T invoke(Object object, String methodeName, Object... args) {
        return (T) MethodUtils.invokeExactMethod(object, methodeName, args);
    }
}
