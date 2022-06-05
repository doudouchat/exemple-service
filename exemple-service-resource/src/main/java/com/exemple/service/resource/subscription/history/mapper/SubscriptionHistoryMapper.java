package com.exemple.service.resource.subscription.history.mapper;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.mapper.MapperBuilder;
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory;
import com.datastax.oss.driver.api.mapper.annotations.Mapper;
import com.exemple.service.resource.subscription.history.dao.SubscriptionHistoryDao;

@Mapper
public interface SubscriptionHistoryMapper {

    @DaoFactory
    SubscriptionHistoryDao subscriptionHistoryDao();

    static MapperBuilder<SubscriptionHistoryMapper> builder(CqlSession session) {
        return new SubscriptionHistoryMapperBuilder(session);
    }
}
