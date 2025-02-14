package com.exemple.service.resource.common.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.AbstractAssert;

public class HistoryAssert<T> extends AbstractAssert<HistoryAssert<T>, T> {

    private final List<? extends HistoryModel<T>> histories;

    public HistoryAssert(T id, List<? extends HistoryModel<T>> histories) {
        super(id, HistoryAssert.class);
        this.histories = histories;
    }

    public HistoryAssert<T> contains(List<ExpectedHistory<T>> expectedHistories) {
        isNotNull();

        assertThat(histories).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedHistories.stream()
                        .map(expectedHistory -> expectedHistory.buildHistory(this.actual))
                        .toList());
        return this;
    }

}
