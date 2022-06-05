package com.exemple.service.resource.subscription.model;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.exemple.service.resource.common.history.HistoryModel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@CqlName("subscription_history")
public class SubscriptionHistory extends HistoryModel<String> {

    @PartitionKey
    @CqlName("email")
    private String id;

}
