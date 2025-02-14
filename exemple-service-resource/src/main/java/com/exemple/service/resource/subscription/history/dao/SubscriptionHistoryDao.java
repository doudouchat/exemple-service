package com.exemple.service.resource.subscription.history.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.subscription.model.SubscriptionHistory;

@Dao
public interface SubscriptionHistoryDao {

    @Insert
    BoundStatement save(SubscriptionHistory subscriptionHistory);

    @Select
    PagingIterable<SubscriptionHistory> findById(String email);

    @Delete(entityClass = SubscriptionHistory.class)
    BoundStatement deleteByIdAndField(String email, String field);
}
