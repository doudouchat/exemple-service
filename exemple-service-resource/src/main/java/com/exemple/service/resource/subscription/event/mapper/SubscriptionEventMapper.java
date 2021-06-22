package com.exemple.service.resource.subscription.event.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.subscription.event.dao.SubscriptionEventDao;

@Mapper
public interface SubscriptionEventMapper {

    @DaoFactory
    SubscriptionEventDao subscriptionEventDao();

    static MapperBuilder<SubscriptionEventMapper> builder(CqlSession session) {
        return new SubscriptionEventMapperBuilder(session);
    }
}
