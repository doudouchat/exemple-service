package com.exemple.service.resource.subscription.event.dao;

import java.time.Instant;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.subscription.model.SubscriptionEvent;

@Dao
public interface SubscriptionEventDao {

    @Insert
    BoundStatement create(SubscriptionEvent subscriptionEvent);

    @Select
    SubscriptionEvent getByIdAndDate(String email, Instant date);

}
