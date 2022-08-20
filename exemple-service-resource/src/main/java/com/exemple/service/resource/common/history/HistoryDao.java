package com.exemple.service.resource.common.history;

import java.util.function.Supplier;

import org.apache.commons.lang3.reflect.MethodUtils;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;

import lombok.SneakyThrows;

@FunctionalInterface
public interface HistoryDao<I, T extends HistoryModel<I>> extends Supplier<Object> {

    default BoundStatement save(T historyModel) {
        return invoke("save", historyModel);
    }

    default PagingIterable<T> findById(I id) {
        return invoke("findById", id);
    }

    default BoundStatement deleteByIdAndField(I id, String field) {
        return invoke("deleteByIdAndField", id, field);
    }

    @SneakyThrows
    private <O> O invoke(String methodeName, Object... args) {
        return (O) MethodUtils.invokeExactMethod(get(), methodeName, args);
    }
}
