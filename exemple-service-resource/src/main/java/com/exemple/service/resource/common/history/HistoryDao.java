package com.exemple.service.resource.common.history;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;

import lombok.SneakyThrows;

@FunctionalInterface
public interface HistoryDao<I, T extends HistoryModel<I>> {

    Object dao();

    default BoundStatement save(T historyModel) {
        return invoke(dao(), "save", historyModel);
    }

    default PagingIterable<T> findById(I id) {
        return invoke(dao(), "findById", id);
    }

    default BoundStatement deleteByIdAndField(I id, String field) {
        return invoke(dao(), "deleteByIdAndField", id, field);
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    static <T> T invoke(Object object, String methodeName, Object... args) {
        return (T) MethodUtils.invokeExactMethod(object, methodeName, args);
    }
}
